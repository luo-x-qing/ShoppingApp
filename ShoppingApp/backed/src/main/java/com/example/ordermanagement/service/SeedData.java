package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Attraction;

import java.util.ArrayList;
import java.util.List;

public class SeedData {

    public static List<Attraction> getAttractions() {
        List<Attraction> list = new ArrayList<>();

        list.add(create("故宫博物院", "北京市", "北京市", 60.0, "历史古迹", "AAAAA", "08:30-17:00"));
        list.add(create("天坛公园", "北京市", "北京市", 15.0, "历史古迹", "AAAAA", "06:00-21:00"));
        list.add(create("颐和园", "北京市", "北京市", 30.0, "古典园林", "AAAAA", "06:30-18:00"));
        list.add(create("八达岭长城", "北京市", "北京市", 40.0, "历史古迹", "AAAAA", "07:30-16:00"));
        list.add(create("上海迪士尼乐园", "上海市", "上海市", 399.0, "主题乐园", "AAAAA", "08:30-20:30"));
        list.add(create("东方明珠塔", "上海市", "上海市", 180.0, "现代建筑", "AAAAA", "08:00-22:00"));
        list.add(create("外滩", "上海市", "上海市", 0.0, "城市景观", "AAAAA", "全天开放"));
        list.add(create("南京路步行街", "上海市", "上海市", 0.0, "商业街区", "AAAA", "全天开放"));
        list.add(create("广州塔", "广东省", "广州市", 150.0, "现代建筑", "AAAAA", "09:00-22:30"));
        list.add(create("长隆欢乐世界", "广东省", "广州市", 250.0, "主题乐园", "AAAAA", "09:30-18:00"));
        list.add(create("白云山风景区", "广东省", "广州市", 5.0, "自然风光", "AAAAA", "06:00-21:00"));
        list.add(create("世界之窗", "广东省", "深圳市", 220.0, "主题乐园", "AAAAA", "09:00-22:00"));
        list.add(create("欢乐谷", "广东省", "深圳市", 230.0, "主题乐园", "AAAAA", "09:30-21:00"));
        list.add(create("西湖风景区", "浙江省", "杭州市", 0.0, "自然风光", "AAAAA", "全天开放"));
        list.add(create("宋城", "浙江省", "杭州市", 310.0, "主题乐园", "AAAAA", "09:00-21:00"));
        list.add(create("千岛湖", "浙江省", "杭州市", 130.0, "自然风光", "AAAAA", "08:00-17:00"));
        list.add(create("灵隐寺", "浙江省", "杭州市", 75.0, "宗教文化", "AAAAA", "07:00-18:00"));
        list.add(create("九寨沟", "四川省", "阿坝藏族羌族自治州", 169.0, "自然风光", "AAAAA", "08:30-17:00"));
        list.add(create("峨眉山", "四川省", "乐山市", 160.0, "自然风光", "AAAAA", "06:00-18:00"));
        list.add(create("乐山大佛", "四川省", "乐山市", 80.0, "历史古迹", "AAAAA", "07:30-18:00"));
        list.add(create("青城山", "四川省", "成都市", 80.0, "自然风光", "AAAAA", "08:00-17:00"));
        list.add(create("都江堰", "四川省", "成都市", 80.0, "历史古迹", "AAAAA", "08:00-18:00"));
        list.add(create("宽窄巷子", "四川省", "成都市", 0.0, "历史街区", "AAAA", "全天开放"));
        list.add(create("大熊猫繁育研究基地", "四川省", "成都市", 55.0, "动物园区", "AAAAA", "07:30-18:00"));
        list.add(create("张家界国家森林公园", "湖南省", "张家界市", 228.0, "自然风光", "AAAAA", "08:00-18:00"));
        list.add(create("岳麓山", "湖南省", "长沙市", 0.0, "自然风光", "AAAAA", "06:00-23:00"));
        list.add(create("橘子洲头", "湖南省", "长沙市", 0.0, "自然风光", "AAAAA", "07:00-22:00"));
        list.add(create("岳阳楼", "湖南省", "岳阳市", 70.0, "历史古迹", "AAAAA", "07:00-18:00"));
        list.add(create("鼓浪屿", "福建省", "厦门市", 35.0, "海岛风光", "AAAAA", "全天开放"));
        list.add(create("武夷山", "福建省", "南平市", 140.0, "自然风光", "AAAAA", "08:00-17:00"));
        list.add(create("黄山", "安徽省", "黄山市", 190.0, "自然风光", "AAAAA", "06:00-17:30"));
        list.add(create("宏村", "安徽省", "黄山市", 104.0, "古村镇", "AAAAA", "07:30-17:30"));
        list.add(create("泰山", "山东省", "泰安市", 115.0, "自然风光", "AAAAA", "全天开放"));
        list.add(create("曲阜孔庙", "山东省", "济宁市", 140.0, "历史古迹", "AAAAA", "08:00-17:00"));
        list.add(create("崂山", "山东省", "青岛市", 80.0, "自然风光", "AAAAA", "07:00-17:30"));
        list.add(create("西安兵马俑", "陕西省", "西安市", 120.0, "历史古迹", "AAAAA", "08:30-17:00"));
        list.add(create("大雁塔", "陕西省", "西安市", 40.0, "历史古迹", "AAAAA", "08:00-18:00"));
        list.add(create("华山", "陕西省", "渭南市", 160.0, "自然风光", "AAAAA", "07:00-18:00"));
        list.add(create("丽江古城", "云南省", "丽江市", 50.0, "古村镇", "AAAAA", "全天开放"));
        list.add(create("玉龙雪山", "云南省", "丽江市", 100.0, "自然风光", "AAAAA", "07:00-17:00"));
        list.add(create("大理古城", "云南省", "大理白族自治州", 0.0, "古村镇", "AAAAA", "全天开放"));
        list.add(create("石林风景区", "云南省", "昆明市", 130.0, "自然风光", "AAAAA", "07:30-18:00"));
        list.add(create("漓江风景区", "广西壮族自治区", "桂林市", 80.0, "自然风光", "AAAAA", "08:00-17:00"));
        list.add(create("桂林阳朔", "广西壮族自治区", "桂林市", 0.0, "自然风光", "AAAAA", "全天开放"));
        list.add(create("黄果树瀑布", "贵州省", "安顺市", 160.0, "自然风光", "AAAAA", "07:30-18:00"));
        list.add(create("黔灵山公园", "贵州省", "贵阳市", 5.0, "自然风光", "AAAAA", "06:30-22:00"));
        list.add(create("庐山", "江西省", "九江市", 160.0, "自然风光", "AAAAA", "07:00-18:00"));
        list.add(create("三清山", "江西省", "上饶市", 120.0, "自然风光", "AAAAA", "08:00-17:00"));
        list.add(create("中山陵", "江苏省", "南京市", 0.0, "历史古迹", "AAAAA", "08:30-17:00"));
        list.add(create("夫子庙秦淮河", "江苏省", "南京市", 0.0, "历史街区", "AAAAA", "全天开放"));
        list.add(create("苏州园林", "江苏省", "苏州市", 80.0, "古典园林", "AAAAA", "07:30-17:30"));
        list.add(create("周庄古镇", "江苏省", "苏州市", 100.0, "古村镇", "AAAAA", "08:00-17:00"));
        list.add(create("武当山", "湖北省", "十堰市", 130.0, "自然风光", "AAAAA", "07:00-17:00"));
        list.add(create("黄鹤楼", "湖北省", "武汉市", 70.0, "历史古迹", "AAAAA", "08:00-18:00"));
        list.add(create("东湖风景区", "湖北省", "武汉市", 0.0, "自然风光", "AAAAA", "全天开放"));
        list.add(create("少林寺", "河南省", "郑州市", 100.0, "宗教文化", "AAAAA", "08:00-17:00"));
        list.add(create("龙门石窟", "河南省", "洛阳市", 90.0, "历史古迹", "AAAAA", "08:00-18:00"));
        list.add(create("老君山", "河南省", "洛阳市", 100.0, "自然风光", "AAAAA", "08:00-18:00"));
        list.add(create("承德避暑山庄", "河北省", "承德市", 130.0, "历史古迹", "AAAAA", "07:00-18:00"));
        list.add(create("北戴河", "河北省", "秦皇岛市", 0.0, "海滨风光", "AAAAA", "全天开放"));
        list.add(create("五台山", "山西省", "忻州市", 135.0, "宗教文化", "AAAAA", "06:30-18:00"));
        list.add(create("平遥古城", "山西省", "晋中市", 125.0, "古村镇", "AAAAA", "08:00-18:00"));
        list.add(create("大连老虎滩海洋公园", "辽宁省", "大连市", 220.0, "主题乐园", "AAAAA", "08:30-17:00"));
        list.add(create("哈尔滨冰雪大世界", "黑龙江省", "哈尔滨市", 330.0, "主题乐园", "AAAA", "11:00-21:30"));
        list.add(create("长白山天池", "吉林省", "延边朝鲜族自治州", 105.0, "自然风光", "AAAAA", "07:00-17:00"));
        list.add(create("呼伦贝尔草原", "内蒙古自治区", "呼伦贝尔市", 0.0, "草原风光", "AAAAA", "全天开放"));
        list.add(create("敦煌莫高窟", "甘肃省", "酒泉市", 238.0, "历史古迹", "AAAAA", "08:00-18:00"));
        list.add(create("嘉峪关", "甘肃省", "嘉峪关市", 110.0, "历史古迹", "AAAAA", "08:00-18:00"));
        list.add(create("青海湖", "青海省", "海东市", 90.0, "自然风光", "AAAAA", "08:00-18:00"));
        list.add(create("布达拉宫", "西藏自治区", "拉萨市", 200.0, "宗教文化", "AAAAA", "09:00-16:00"));
        list.add(create("沙坡头", "宁夏回族自治区", "中卫市", 80.0, "自然风光", "AAAAA", "08:00-18:00"));
        list.add(create("天山天池", "新疆维吾尔自治区", "乌鲁木齐市", 95.0, "自然风光", "AAAAA", "08:30-19:00"));
        list.add(create("吐鲁番葡萄沟", "新疆维吾尔自治区", "吐鲁番市", 75.0, "自然风光", "AAAAA", "08:00-20:00"));
        list.add(create("重庆洪崖洞", "重庆市", "重庆市", 0.0, "城市景观", "AAAAA", "11:00-23:00"));
        list.add(create("长江索道", "重庆市", "重庆市", 30.0, "城市景观", "AAAAA", "07:30-21:30"));
        list.add(create("天津之眼", "天津市", "天津市", 70.0, "现代建筑", "AAAAA", "09:00-21:00"));
        list.add(create("三亚天涯海角", "海南省", "三亚市", 81.0, "海滨风光", "AAAAA", "07:30-18:00"));

        return list;
    }

    private static Attraction create(String name, String province, String city, Double ticketPrice, String type, String level, String openTime) {
        Attraction a = new Attraction();
        a.setName(name);
        a.setProvince(province);
        a.setCity(city);
        a.setTicketPrice(ticketPrice);
        a.setType(type);
        a.setLevel(level);
        a.setOpenTime(openTime);
        a.setScore(4.5);
        return a;
    }
}
