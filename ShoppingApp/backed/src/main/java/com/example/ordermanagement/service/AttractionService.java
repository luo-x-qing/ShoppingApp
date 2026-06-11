package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Attraction;
import com.example.ordermanagement.repository.AttractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private AmapService amapService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, Map<String, List<String>>> BUILTIN_SCENIC_SPOTS = new LinkedHashMap<>();

    static {
        Map<String, List<String>> bj = new LinkedHashMap<>();
        bj.put("北京市", Arrays.asList("故宫博物院", "天坛公园", "颐和园", "八达岭长城", "鸟巢", "水立方", "北海公园", "恭王府", "圆明园", "香山公园"));
        BUILTIN_SCENIC_SPOTS.put("北京市", bj);

        Map<String, List<String>> tj = new LinkedHashMap<>();
        tj.put("天津市", Arrays.asList("天津之眼", "古文化街", "五大道", "意式风情区", "盘山风景区", "黄崖关长城", "瓷房子", "天津欢乐谷"));
        BUILTIN_SCENIC_SPOTS.put("天津市", tj);

        Map<String, List<String>> sh = new LinkedHashMap<>();
        sh.put("上海市", Arrays.asList("东方明珠", "外滩", "迪士尼乐园", "豫园", "南京路步行街", "上海科技馆", "城隍庙", "朱家角古镇"));
        BUILTIN_SCENIC_SPOTS.put("上海市", sh);

        Map<String, List<String>> cq = new LinkedHashMap<>();
        cq.put("重庆市", Arrays.asList("洪崖洞", "磁器口古镇", "武隆天生三桥", "解放碑", "长江索道", "大足石刻", "南山一棵树", "仙女山"));
        BUILTIN_SCENIC_SPOTS.put("重庆市", cq);

        Map<String, List<String>> heb = new LinkedHashMap<>();
        heb.put("石家庄市", Arrays.asList("西柏坡", "正定古城", "隆兴寺", "赵州桥", "苍岩山", "荣国府"));
        heb.put("唐山市", Arrays.asList("清东陵", "唐山南湖", "月坨岛", "曹妃甸湿地"));
        heb.put("秦皇岛市", Arrays.asList("山海关", "北戴河", "鸽子窝公园", "老龙头"));
        heb.put("邯郸市", Arrays.asList("娲皇宫", "丛台公园", "京娘湖", "太行五指山"));
        heb.put("保定市", Arrays.asList("野三坡", "白洋淀", "狼牙山", "直隶总督署"));
        BUILTIN_SCENIC_SPOTS.put("河北省", heb);

        Map<String, List<String>> sx = new LinkedHashMap<>();
        sx.put("太原市", Arrays.asList("晋祠", "蒙山大佛", "山西博物院", "双塔寺"));
        sx.put("大同市", Arrays.asList("云冈石窟", "恒山", "悬空寺", "华严寺"));
        sx.put("晋中市", Arrays.asList("平遥古城", "乔家大院", "王家大院", "绵山"));
        sx.put("运城市", Arrays.asList("解州关帝庙", "鹳雀楼", "盐池", "永乐宫"));
        BUILTIN_SCENIC_SPOTS.put("山西省", sx);

        Map<String, List<String>> nmg = new LinkedHashMap<>();
        nmg.put("呼和浩特市", Arrays.asList("大召寺", "昭君博物院", "希拉穆仁草原", "内蒙古博物院"));
        nmg.put("包头市", Arrays.asList("五当召", "赛汗塔拉", "北方兵器城", "南海湿地"));
        nmg.put("赤峰市", Arrays.asList("乌兰布统", "达里诺尔湖", "阿斯哈图石林", "克什克腾旗"));
        BUILTIN_SCENIC_SPOTS.put("内蒙古自治区", nmg);

        Map<String, List<String>> ln = new LinkedHashMap<>();
        ln.put("沈阳市", Arrays.asList("沈阳故宫", "张氏帅府", "北陵公园", "世博园"));
        ln.put("大连市", Arrays.asList("老虎滩海洋公园", "星海广场", "金石滩", "旅顺口"));
        ln.put("鞍山市", Arrays.asList("千山", "玉佛苑", "汤岗子温泉"));
        ln.put("锦州市", Arrays.asList("笔架山", "辽沈战役纪念馆", "医巫闾山"));
        BUILTIN_SCENIC_SPOTS.put("辽宁省", ln);

        Map<String, List<String>> jl = new LinkedHashMap<>();
        jl.put("长春市", Arrays.asList("伪满皇宫", "净月潭", "长影世纪城", "南湖公园"));
        jl.put("吉林市", Arrays.asList("雾凇岛", "松花湖", "北山公园", "龙潭山"));
        jl.put("延边朝鲜族自治州", Arrays.asList("长白山天池", "延边大学", "图们江", "防川风景区"));
        BUILTIN_SCENIC_SPOTS.put("吉林省", jl);

        Map<String, List<String>> hlj = new LinkedHashMap<>();
        hlj.put("哈尔滨市", Arrays.asList("冰雪大世界", "太阳岛", "圣索菲亚教堂", "中央大街"));
        hlj.put("齐齐哈尔市", Arrays.asList("扎龙自然保护区", "明月岛", "龙沙公园"));
        hlj.put("牡丹江市", Arrays.asList("镜泊湖", "雪乡", "绥芬河口岸", "威虎山"));
        BUILTIN_SCENIC_SPOTS.put("黑龙江省", hlj);

        Map<String, List<String>> js = new LinkedHashMap<>();
        js.put("南京市", Arrays.asList("中山陵", "夫子庙", "明孝陵", "总统府", "玄武湖", "南京博物院"));
        js.put("苏州市", Arrays.asList("拙政园", "虎丘", "周庄古镇", "苏州博物馆", "山塘街", "金鸡湖"));
        js.put("无锡市", Arrays.asList("鼋头渚", "灵山大佛", "三国水浒城", "惠山古镇"));
        js.put("扬州市", Arrays.asList("瘦西湖", "个园", "大明寺", "何园"));
        js.put("镇江市", Arrays.asList("金山寺", "焦山", "北固山", "西津渡"));
        js.put("常州市", Arrays.asList("中华恐龙园", "天目湖", "南山竹海", "嬉戏谷"));
        js.put("南通市", Arrays.asList("狼山", "濠河", "南通博物苑", "水绘园"));
        js.put("徐州市", Arrays.asList("云龙山", "云龙湖", "龟山汉墓", "项羽戏马台"));
        BUILTIN_SCENIC_SPOTS.put("江苏省", js);

        Map<String, List<String>> zj = new LinkedHashMap<>();
        zj.put("杭州市", Arrays.asList("西湖", "灵隐寺", "宋城", "千岛湖", "西溪湿地", "雷峰塔"));
        zj.put("宁波市", Arrays.asList("天一阁", "奉化溪口", "东钱湖", "象山影视城"));
        zj.put("温州市", Arrays.asList("雁荡山", "楠溪江", "江心屿", "百丈漈"));
        zj.put("嘉兴市", Arrays.asList("乌镇", "西塘", "南湖", "盐官古镇"));
        zj.put("湖州市", Arrays.asList("南浔古镇", "莫干山", "安吉大竹海", "太湖"));
        zj.put("绍兴市", Arrays.asList("鲁迅故里", "沈园", "兰亭", "东湖"));
        zj.put("金华市", Arrays.asList("横店影视城", "双龙洞", "诸葛八卦村"));
        zj.put("台州市", Arrays.asList("天台山", "神仙居", "长屿硐天", "大陈岛"));
        BUILTIN_SCENIC_SPOTS.put("浙江省", zj);

        Map<String, List<String>> ah = new LinkedHashMap<>();
        ah.put("合肥市", Arrays.asList("三河古镇", "包公园", "徽园", "巢湖"));
        ah.put("黄山市", Arrays.asList("黄山风景区", "宏村", "西递", "屯溪老街", "徽州古城"));
        ah.put("芜湖市", Arrays.asList("方特欢乐世界", "鸠兹古镇", "马仁奇峰"));
        ah.put("池州市", Arrays.asList("九华山", "杏花村", "牯牛降"));
        BUILTIN_SCENIC_SPOTS.put("安徽省", ah);

        Map<String, List<String>> fj = new LinkedHashMap<>();
        fj.put("福州市", Arrays.asList("三坊七巷", "鼓山", "平潭岛", "上下杭"));
        fj.put("厦门市", Arrays.asList("鼓浪屿", "南普陀寺", "曾厝垵", "厦门大学", "环岛路"));
        fj.put("泉州市", Arrays.asList("清源山", "开元寺", "崇武古城", "晋江五店市"));
        fj.put("漳州市", Arrays.asList("南靖土楼", "云水谣", "东山岛", "火山岛"));
        fj.put("龙岩市", Arrays.asList("永定土楼", "古田会议旧址", "冠豸山"));
        BUILTIN_SCENIC_SPOTS.put("福建省", fj);

        Map<String, List<String>> jx = new LinkedHashMap<>();
        jx.put("南昌市", Arrays.asList("滕王阁", "八一起义纪念馆", "梅岭", "鄱阳湖"));
        jx.put("九江市", Arrays.asList("庐山", "浔阳楼", "鄱阳湖湿地", "白鹿洞书院"));
        jx.put("景德镇市", Arrays.asList("古窑民俗博览区", "御窑厂", "陶溪川", "瑶里古镇"));
        jx.put("上饶市", Arrays.asList("三清山", "婺源", "龟峰", "灵山"));
        jx.put("赣州市", Arrays.asList("瑞金", "通天岩", "客家围屋", "丫山"));
        BUILTIN_SCENIC_SPOTS.put("江西省", jx);

        Map<String, List<String>> sd = new LinkedHashMap<>();
        sd.put("济南市", Arrays.asList("趵突泉", "大明湖", "千佛山", "芙蓉街", "灵岩寺"));
        sd.put("青岛市", Arrays.asList("栈桥", "崂山", "五四广场", "八大关", "金沙滩"));
        sd.put("烟台市", Arrays.asList("蓬莱阁", "长岛", "烟台山", "养马岛"));
        sd.put("潍坊市", Arrays.asList("风筝博物馆", "青州古城", "十笏园", "杨家埠"));
        sd.put("泰安市", Arrays.asList("泰山", "岱庙", "太阳部落", "东平湖"));
        sd.put("威海市", Arrays.asList("刘公岛", "成山头", "威海公园", "赤山风景区"));
        sd.put("淄博市", Arrays.asList("周村古商城", "蒲松龄故居", "鲁山", "管仲纪念馆"));
        sd.put("济宁市", Arrays.asList("曲阜三孔", "微山湖", "水泊梁山"));
        BUILTIN_SCENIC_SPOTS.put("山东省", sd);

        Map<String, List<String>> hn = new LinkedHashMap<>();
        hn.put("郑州市", Arrays.asList("少林寺", "黄河风景区", "郑州方特", "河南博物院"));
        hn.put("洛阳市", Arrays.asList("龙门石窟", "白马寺", "老君山", "洛阳牡丹园", "关林"));
        hn.put("开封市", Arrays.asList("清明上河园", "开封府", "大相国寺", "龙亭公园"));
        hn.put("安阳市", Arrays.asList("殷墟", "红旗渠", "太行大峡谷", "羑里城"));
        hn.put("焦作市", Arrays.asList("云台山", "青天河", "神农山", "陈家沟"));
        hn.put("南阳市", Arrays.asList("武侯祠", "南阳汉画馆", "恐龙蛋化石博物馆"));
        hn.put("信阳市", Arrays.asList("鸡公山", "南湾湖", "灵山寺"));
        BUILTIN_SCENIC_SPOTS.put("河南省", hn);

        Map<String, List<String>> hb = new LinkedHashMap<>();
        hb.put("武汉市", Arrays.asList("黄鹤楼", "东湖", "武汉长江大桥", "户部巷", "归元寺", "汉口江滩"));
        hb.put("宜昌市", Arrays.asList("三峡大坝", "三峡人家", "清江画廊", "屈原故里"));
        hb.put("襄阳市", Arrays.asList("古隆中", "襄阳古城", "唐城影视基地", "米公祠"));
        hb.put("荆州市", Arrays.asList("荆州古城", "荆州博物馆", "洈水风景区"));
        hb.put("十堰市", Arrays.asList("武当山", "丹江口水库", "神农架"));
        hb.put("恩施土家族苗族自治州", Arrays.asList("恩施大峡谷", "腾龙洞", "土司城", "屏山峡谷"));
        BUILTIN_SCENIC_SPOTS.put("湖北省", hb);

        Map<String, List<String>> hun = new LinkedHashMap<>();
        hun.put("长沙市", Arrays.asList("岳麓山", "橘子洲", "湖南省博物馆", "太平街", "天心阁"));
        hun.put("张家界市", Arrays.asList("张家界国家森林公园", "天门山", "大峡谷", "黄龙洞"));
        hun.put("衡阳市", Arrays.asList("衡山", "南岳大庙", "石鼓书院"));
        hun.put("湘潭市", Arrays.asList("韶山", "乌石", "盘龙大观园"));
        hun.put("岳阳市", Arrays.asList("岳阳楼", "君山岛", "洞庭湖", "汨罗江"));
        hun.put("湘西土家族苗族自治州", Arrays.asList("凤凰古城", "芙蓉镇", "矮寨大桥", "德夯苗寨"));
        BUILTIN_SCENIC_SPOTS.put("湖南省", hun);

        Map<String, List<String>> gd = new LinkedHashMap<>();
        gd.put("广州市", Arrays.asList("广州塔", "白云山", "长隆欢乐世界", "沙面", "陈家祠", "越秀公园"));
        gd.put("深圳市", Arrays.asList("世界之窗", "东部华侨城", "欢乐谷", "大小梅沙", "深圳湾公园"));
        gd.put("珠海市", Arrays.asList("长隆海洋王国", "情侣路", "圆明新园", "珠海渔女"));
        gd.put("汕头市", Arrays.asList("南澳岛", "中山公园", "澄海陈慈黉故居"));
        gd.put("佛山市", Arrays.asList("西樵山", "祖庙", "清晖园", "南风古灶"));
        gd.put("东莞市", Arrays.asList("可园", "松山湖", "虎门炮台", "观音山"));
        gd.put("韶关市", Arrays.asList("丹霞山", "南华寺", "珠玑古巷"));
        gd.put("惠州市", Arrays.asList("罗浮山", "惠州西湖", "巽寮湾", "双月湾"));
        gd.put("中山市", Arrays.asList("孙中山故居", "詹园", "中山温泉"));
        BUILTIN_SCENIC_SPOTS.put("广东省", gd);

        Map<String, List<String>> gx = new LinkedHashMap<>();
        gx.put("南宁市", Arrays.asList("青秀山", "南湖公园", "广西民族博物馆", "大明山"));
        gx.put("桂林市", Arrays.asList("漓江", "阳朔西街", "象鼻山", "龙脊梯田", "十里画廊"));
        gx.put("北海市", Arrays.asList("银滩", "涠洲岛", "老街", "海洋之窗"));
        gx.put("柳州市", Arrays.asList("龙潭公园", "柳侯祠", "马鞍山", "三江程阳风雨桥"));
        gx.put("百色市", Arrays.asList("乐业天坑", "百色起义纪念馆", "通灵大峡谷"));
        BUILTIN_SCENIC_SPOTS.put("广西壮族自治区", gx);

        Map<String, List<String>> hain = new LinkedHashMap<>();
        hain.put("海口市", Arrays.asList("骑楼老街", "火山口公园", "假日海滩", "东寨港"));
        hain.put("三亚市", Arrays.asList("亚龙湾", "天涯海角", "南山寺", "蜈支洲岛", "大小洞天"));
        BUILTIN_SCENIC_SPOTS.put("海南省", hain);

        Map<String, List<String>> sc = new LinkedHashMap<>();
        sc.put("成都市", Arrays.asList("都江堰", "青城山", "大熊猫基地", "锦里", "宽窄巷子", "杜甫草堂", "武侯祠", "金沙遗址"));
        sc.put("乐山市", Arrays.asList("乐山大佛", "峨眉山", "东风堰", "嘉阳小火车"));
        sc.put("绵阳市", Arrays.asList("北川羌城", "窦圌山", "李白故里", "九皇山"));
        sc.put("泸州市", Arrays.asList("泸州老窖景区", "黄荆老林", "太平古镇"));
        sc.put("宜宾市", Arrays.asList("蜀南竹海", "李庄古镇", "兴文石海"));
        sc.put("阿坝藏族羌族自治州", Arrays.asList("九寨沟", "黄龙", "四姑娘山", "若尔盖草原", "达古冰川"));
        sc.put("甘孜藏族自治州", Arrays.asList("稻城亚丁", "海螺沟", "色达佛学院", "康定情歌城"));
        sc.put("南充市", Arrays.asList("阆中古城", "朱德故里", "凌云山"));
        BUILTIN_SCENIC_SPOTS.put("四川省", sc);

        Map<String, List<String>> gz = new LinkedHashMap<>();
        gz.put("贵阳市", Arrays.asList("黔灵山", "甲秀楼", "青岩古镇", "花溪公园"));
        gz.put("遵义市", Arrays.asList("遵义会议会址", "赤水丹霞", "茅台镇", "娄山关"));
        gz.put("安顺市", Arrays.asList("黄果树瀑布", "龙宫", "格凸河", "屯堡"));
        gz.put("黔东南苗族侗族自治州", Arrays.asList("西江千户苗寨", "镇远古城", "肇兴侗寨", "加榜梯田"));
        gz.put("黔南布依族苗族自治州", Arrays.asList("荔波小七孔", "茂兰自然保护区", "平塘天眼"));
        BUILTIN_SCENIC_SPOTS.put("贵州省", gz);

        Map<String, List<String>> yn = new LinkedHashMap<>();
        yn.put("昆明市", Arrays.asList("石林", "滇池", "西山龙门", "云南民族村", "金马碧鸡坊"));
        yn.put("大理白族自治州", Arrays.asList("大理古城", "洱海", "崇圣寺三塔", "双廊古镇"));
        yn.put("丽江市", Arrays.asList("玉龙雪山", "丽江古城", "泸沽湖", "束河古镇"));
        yn.put("迪庆藏族自治州", Arrays.asList("普达措", "松赞林寺", "独克宗古城", "梅里雪山"));
        yn.put("西双版纳傣族自治州", Arrays.asList("告庄西双景", "曼听公园", "中科院植物园", "傣族园"));
        yn.put("曲靖市", Arrays.asList("罗平油菜花", "陆良彩色沙林", "珠江源"));
        yn.put("保山市", Arrays.asList("腾冲热海", "和顺古镇", "火山地质公园"));
        BUILTIN_SCENIC_SPOTS.put("云南省", yn);

        Map<String, List<String>> xz = new LinkedHashMap<>();
        xz.put("拉萨市", Arrays.asList("布达拉宫", "大昭寺", "八廓街", "纳木错"));
        xz.put("林芝市", Arrays.asList("巴松措", "雅鲁藏布大峡谷", "南迦巴瓦峰", "鲁朗林海"));
        xz.put("日喀则市", Arrays.asList("珠穆朗玛峰", "扎什伦布寺", "白居寺"));
        BUILTIN_SCENIC_SPOTS.put("西藏自治区", xz);

        Map<String, List<String>> shx = new LinkedHashMap<>();
        shx.put("西安市", Arrays.asList("兵马俑", "大雁塔", "华清池", "西安城墙", "回民街", "钟鼓楼", "陕西历史博物馆"));
        shx.put("宝鸡市", Arrays.asList("法门寺", "太白山", "关山草原", "青铜器博物院"));
        shx.put("咸阳市", Arrays.asList("乾陵", "茂陵", "汉阳陵", "马嵬驿"));
        shx.put("渭南市", Arrays.asList("华山", "韩城司马迁祠", "洽川湿地"));
        shx.put("延安市", Arrays.asList("宝塔山", "壶口瀑布", "黄帝陵", "杨家岭"));
        shx.put("汉中市", Arrays.asList("古汉台", "武侯墓", "黎坪森林公园", "青木川"));
        BUILTIN_SCENIC_SPOTS.put("陕西省", shx);

        Map<String, List<String>> gs = new LinkedHashMap<>();
        gs.put("兰州市", Arrays.asList("中山桥", "白塔山", "五泉山", "甘肃省博物馆"));
        gs.put("天水市", Arrays.asList("麦积山石窟", "伏羲庙", "南郭寺", "仙人崖"));
        gs.put("酒泉市", Arrays.asList("莫高窟", "鸣沙山月牙泉", "嘉峪关", "敦煌古城"));
        gs.put("张掖市", Arrays.asList("七彩丹霞", "大佛寺", "马蹄寺"));
        gs.put("甘南藏族自治州", Arrays.asList("拉卜楞寺", "扎尕那", "郎木寺", "桑科草原"));
        BUILTIN_SCENIC_SPOTS.put("甘肃省", gs);

        Map<String, List<String>> qh = new LinkedHashMap<>();
        qh.put("西宁市", Arrays.asList("塔尔寺", "青海湖", "茶卡盐湖", "东关清真大寺"));
        qh.put("海东市", Arrays.asList("互助土族故土园", "孟达天池"));
        BUILTIN_SCENIC_SPOTS.put("青海省", qh);

        Map<String, List<String>> nx = new LinkedHashMap<>();
        nx.put("银川市", Arrays.asList("沙湖", "西夏王陵", "镇北堡西部影城", "贺兰山岩画"));
        nx.put("石嘴山市", Arrays.asList("沙湖", "北武当生态旅游区"));
        nx.put("吴忠市", Arrays.asList("青铜峡黄河大峡谷", "盐池古城"));
        BUILTIN_SCENIC_SPOTS.put("宁夏回族自治区", nx);

        Map<String, List<String>> xj = new LinkedHashMap<>();
        xj.put("乌鲁木齐市", Arrays.asList("天山天池", "红山公园", "大巴扎", "水磨沟公园"));
        xj.put("吐鲁番市", Arrays.asList("火焰山", "葡萄沟", "坎儿井", "交河故城"));
        xj.put("伊犁哈萨克自治州", Arrays.asList("那拉提草原", "赛里木湖", "果子沟大桥", "喀拉峻草原"));
        xj.put("喀什地区", Arrays.asList("喀什古城", "香妃园", "艾提尕尔清真寺"));
        xj.put("克拉玛依市", Arrays.asList("世界魔鬼城", "黑油山"));
        BUILTIN_SCENIC_SPOTS.put("新疆维吾尔自治区", xj);

        Map<String, List<String>> tw = new LinkedHashMap<>();
        tw.put("台北市", Arrays.asList("台北101", "故宫博物院", "士林夜市", "阳明山"));
        tw.put("高雄市", Arrays.asList("爱河", "西子湾", "佛光山", "六合夜市"));
        tw.put("台中市", Arrays.asList("逢甲夜市", "彩虹眷村", "高美湿地", "武陵农场"));
        tw.put("台南市", Arrays.asList("安平古堡", "赤崁楼", "花园夜市", "神农街"));
        BUILTIN_SCENIC_SPOTS.put("台湾省", tw);

        Map<String, List<String>> hk = new LinkedHashMap<>();
        hk.put("香港", Arrays.asList("迪士尼乐园", "海洋公园", "太平山顶", "维多利亚港", "星光大道", "旺角"));
        BUILTIN_SCENIC_SPOTS.put("香港特别行政区", hk);

        Map<String, List<String>> mc = new LinkedHashMap<>();
        mc.put("澳门", Arrays.asList("大三巴牌坊", "威尼斯人", "澳门塔", "渔人码头", "官也街"));
        BUILTIN_SCENIC_SPOTS.put("澳门特别行政区", mc);
    }

    // 查询所有景点
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    // 根据ID查询景点
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id).orElse(null);
    }

    // 按省份查询景点
    public List<Attraction> getAttractionsByProvince(String province) {
        return attractionRepository.findByProvince(province);
    }

    // 按城市查询景点
    public List<Attraction> getAttractionsByCity(String city) {
        return attractionRepository.findByCity(city);
    }

    // 按省份和城市查询景点
    public List<Attraction> getAttractionsByProvinceAndCity(String province, String city) {
        return attractionRepository.findByProvinceAndCity(province, city);
    }

    // 新增/更新景点（自动补全城市）
    public Attraction saveAttraction(Attraction attraction) {
        if ((attraction.getCity() == null || attraction.getCity().isEmpty())
                && attraction.getName() != null && !attraction.getName().isEmpty()) {
            String address = attraction.getName();
            if (attraction.getProvince() != null && !attraction.getProvince().isEmpty()) {
                address = attraction.getName() + "," + attraction.getProvince();
            }
            Map<String, String> detail = amapService.geoDetail(address);
            if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                attraction.setCity(detail.get("city"));
            }
        }
        return attractionRepository.save(attraction);
    }

    // 批量自动补全城市（通过高德API）
    public int autoFillCity() {
        List<Attraction> all = attractionRepository.findAll();
        int count = 0;
        for (Attraction a : all) {
            if (a.getCity() == null || a.getCity().isEmpty()) {
                String address = a.getName();
                if (a.getProvince() != null && !a.getProvince().isEmpty()) {
                    address = a.getName() + "," + a.getProvince();
                }
                Map<String, String> detail = amapService.geoDetail(address);
                if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                    a.setCity(detail.get("city"));
                    attractionRepository.save(a);
                    count++;
                }
            }
        }
        return count;
    }

    // 删除景点
    public void deleteAttraction(Long id) {
        attractionRepository.deleteById(id);
    }

    // 按省份搜索景点
    public List<Attraction> searchByProvinceAndName(String province, String name){
        return attractionRepository.findByProvinceAndNameContaining(province, name);
    }

    // 按城市搜索景点
    public List<Attraction> searchByCityAndName(String city, String name){
        return attractionRepository.findByCityAndNameContaining(city, name);
    }

    // —————————————— 全国景点导入（高德API + 内置数据兜底） ——————————————

    public Map<String, Object> importCityAttractions(String province, String city) {
        int added = 0, skipped = 0, total = 0;
        List<Map<String, String>> pois = amapService.searchScenicByCity(city);
        if (pois == null || pois.isEmpty()) {
            Map<String, List<String>> cities = BUILTIN_SCENIC_SPOTS.get(province);
            if (cities != null) {
                List<String> spots = cities.get(city);
                if (spots != null) {
                    for (String name : spots) {
                        List<Attraction> exist = attractionRepository.findByProvinceAndNameContaining(province, name);
                        if (exist.isEmpty()) {
                            Attraction a = new Attraction();
                            a.setName(name);
                            a.setProvince(province);
                            a.setCity(city);
                            a.setScore(4.0);
                            attractionRepository.save(a);
                            added++;
                        } else {
                            skipped++;
                        }
                        total++;
                    }
                }
            }
        } else {
            for (Map<String, String> poi : pois) {
                String name = poi.get("name");
                String pname = poi.getOrDefault("pname", province);
                String cname = poi.getOrDefault("cityname", city);
                if (name == null || name.isEmpty()) continue;
                List<Attraction> exist = attractionRepository.findByProvinceAndNameContaining(pname, name);
                if (exist.isEmpty()) {
                    Attraction a = new Attraction();
                    a.setName(name);
                    a.setProvince(pname);
                    a.setCity(cname);
                    a.setDescription(poi.getOrDefault("address", ""));
                    a.setScore(4.0);
                    attractionRepository.save(a);
                    added++;
                } else {
                    skipped++;
                }
                total++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("province", province);
        result.put("city", city);
        result.put("total", total);
        result.put("added", added);
        result.put("skipped", skipped);
        return result;
    }

    public List<Map<String, Object>> importAll() {
        List<Map<String, Object>> results = new ArrayList<>();
        int totalAdded = 0, totalSkipped = 0;
        for (Map.Entry<String, Map<String, List<String>>> provEntry : BUILTIN_SCENIC_SPOTS.entrySet()) {
            String province = provEntry.getKey();
            for (String city : provEntry.getValue().keySet()) {
                Map<String, Object> r = importCityAttractions(province, city);
                results.add(r);
                totalAdded += (int) r.get("added");
                totalSkipped += (int) r.get("skipped");
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("province", "汇总");
        summary.put("city", "全国");
        summary.put("total", totalAdded + totalSkipped);
        summary.put("added", totalAdded);
        summary.put("skipped", totalSkipped);
        results.add(summary);
        return results;
    }

    public Map<String, Object> resetAndImportAll() {
        attractionRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE attraction AUTO_INCREMENT = 1");
        List<Map<String, Object>> results = importAll();
        Map<String, Object> summary = results.get(results.size() - 1);
        summary.put("message", "已重置数据库，ID从1开始，共导入 " + summary.get("added") + " 个景点");
        return summary;
    }
}