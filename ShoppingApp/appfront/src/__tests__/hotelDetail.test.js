import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { resetUniMock } from './uni.mock.js'
import HotelDetailPage from '../../pages/hotelDetail/hotelDetail.vue'

function createWrapper() {
  return shallowMount(HotelDetailPage, {
    global: {}
  })
}

describe('HotelDetailPage (酒店详情页)', () => {
  let wrapper

  beforeEach(() => {
    resetUniMock(globalThis.uni)
    wrapper = createWrapper()
  })

  // ========== 初始状态 ==========
  describe('初始状态', () => {
    it('数据应初始化为默认值', () => {
      expect(wrapper.vm.hotel).toEqual({})
      expect(wrapper.vm.images).toEqual([])
      expect(wrapper.vm.roomTypes).toEqual([])
      expect(wrapper.vm.commentList).toEqual([])
      expect(wrapper.vm.isCollected).toBe(false)
      expect(wrapper.vm.avgScore).toBe(0)
    })
  })

  // ========== onLoad ==========
  describe('onLoad - 页面加载', () => {
    function callOnLoad(options) {
      wrapper.vm.$options.onLoad.call(wrapper.vm, options)
    }

    it('应读取 URL 参数中的 id', () => {
      callOnLoad({ id: '123' })
      expect(wrapper.vm.id).toBe('123')
    })

    it('应发起酒店详情、房型和评论请求', () => {
      callOnLoad({ id: '1' })
      const reqCalls = globalThis.uni.request.mock.calls
      expect(reqCalls.length).toBe(3)
    })

    it('应检查收藏状态', () => {
      callOnLoad({ id: '1' })
      expect(globalThis.uni.getStorageSync).toHaveBeenCalled()
    })
  })

  // ========== getHotel - 酒店详情 ==========
  describe('getHotel - 获取酒店详情', () => {
    it('应正确解析标准接口返回', () => {
      wrapper.vm.id = 1
      wrapper.vm.getHotel()

      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({
        data: {
          code: 200,
          data: {
            id: 1,
            name: '测试酒店',
            starLevel: 5,
            price: 299,
            address: '测试地址',
            merchantId: 10
          }
        }
      })

      expect(wrapper.vm.hotel.name).toBe('测试酒店')
      expect(wrapper.vm.hotel.starLevel).toBe(5)
      expect(wrapper.vm.hotel.merchantId).toBe(10)
    })

    it('应处理直接数据返回（非标准格式）', () => {
      wrapper.vm.id = 1
      wrapper.vm.getHotel()

      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({
        data: { id: 1, name: '直接酒店', price: 199 }
      })

      expect(wrapper.vm.hotel.name).toBe('直接酒店')
    })

    it('请求失败应显示提示', () => {
      wrapper.vm.id = 1
      wrapper.vm.getHotel()

      const failCb = globalThis.uni.request.mock.calls[0][0].fail
      failCb(new Error('Error'))

      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '加载失败',
        icon: 'none'
      })
    })
  })

  // ========== 图片处理 ==========
  describe('图片处理', () => {
    it('应处理 detailImages 数组', () => {
      wrapper.vm.hotel = {
        detailImages: [
          { imageUrl: '/uploads/img1.jpg' },
          { imageUrl: '/uploads/img2.jpg' }
        ]
      }
      wrapper.vm.getHotel = vi.fn()
      wrapper.vm.id = 1
      wrapper.vm.getHotel()

      const hotelData = {
        detailImages: [
          { imageUrl: '/uploads/img1.jpg' },
          { imageUrl: '/uploads/img2.jpg' }
        ]
      }
      wrapper.vm.hotel = hotelData
      if (wrapper.vm.hotel.detailImages && wrapper.vm.hotel.detailImages.length > 0) {
        wrapper.vm.images = wrapper.vm.hotel.detailImages.map(img => {
          if (img.imageUrl) return 'http://localhost:8080' + img.imageUrl
          return img
        })
      }
      expect(wrapper.vm.images).toEqual([
        'http://localhost:8080/uploads/img1.jpg',
        'http://localhost:8080/uploads/img2.jpg'
      ])
    })

    it('无 detailImages 时使用单张 image', () => {
      wrapper.vm.hotel = { image: '/uploads/cover.jpg' }
      wrapper.vm.images = ['http://localhost:8080' + wrapper.vm.hotel.image]
      expect(wrapper.vm.images).toEqual(['http://localhost:8080/uploads/cover.jpg'])
    })

    it('无任何图片时使用默认图片', () => {
      wrapper.vm.hotel = {}
      wrapper.vm.images = ['/static/default-hotel.png']
      expect(wrapper.vm.images).toEqual(['/static/default-hotel.png'])
    })
  })

  // ========== getRoomTypes - 房型列表 ==========
  describe('getRoomTypes - 房型列表', () => {
    it('标准格式返回应正确解析', () => {
      wrapper.vm.id = 1
      wrapper.vm.getRoomTypes()

      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({
        data: {
          code: 200,
          data: [
            { id: 1, typeName: '大床房', price: 399, availableCount: 5 },
            { id: 2, typeName: '双床房', price: 499, availableCount: 3 }
          ]
        }
      })

      expect(wrapper.vm.roomTypes.length).toBe(2)
      expect(wrapper.vm.roomTypes[0].typeName).toBe('大床房')
    })
  })

  // ========== getComments - 评价 ==========
  describe('getComments - 评价列表', () => {
    it('应正确解析评价数据', () => {
      wrapper.vm.id = 1
      wrapper.vm.getComments()

      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({
        data: {
          code: 200,
          data: [
            { id: 1, username: '用户A', score: 5, content: '很好', createTime: '2024-01-15' },
            { id: 2, username: '用户B', score: 4, content: '不错', createTime: '2024-01-20' }
          ]
        }
      })

      expect(wrapper.vm.commentList.length).toBe(2)
      expect(wrapper.vm.avgScore).toBe('4.5')
    })

    it('无评价时 avgScore 为 0', () => {
      wrapper.vm.commentList = []
      wrapper.vm.calcAvgScore()
      expect(wrapper.vm.avgScore).toBe(0)
    })

    it('请求失败时 commentList 为空数组', () => {
      wrapper.vm.id = 1
      wrapper.vm.getComments()

      const failCb = globalThis.uni.request.mock.calls[0][0].fail
      failCb(new Error('Error'))

      expect(wrapper.vm.commentList).toEqual([])
    })
  })

  // ========== calcAvgScore - 评分计算 ==========
  describe('calcAvgScore - 平均分计算', () => {
    it('应正确计算平均分', () => {
      wrapper.vm.commentList = [
        { score: 5 }, { score: 4 }, { score: 3 }
      ]
      wrapper.vm.calcAvgScore()
      expect(wrapper.vm.avgScore).toBe('4.0')
    })

    it('带 rating 字段的评论也应正确处理', () => {
      wrapper.vm.commentList = [
        { rating: 5 }, { rating: 5 }
      ]
      wrapper.vm.calcAvgScore()
      expect(wrapper.vm.avgScore).toBe('5.0')
    })

    it('score 为空时默认 5 分', () => {
      wrapper.vm.commentList = [
        { content: '好' },
        { content: '一般' }
      ]
      wrapper.vm.calcAvgScore()
      expect(wrapper.vm.avgScore).toBe('5.0')
    })
  })

  // ========== 收藏功能 ==========
  describe('toggleCollect - 收藏切换', () => {
    it('未登录时应提示登录', () => {
      wrapper.vm.username = ''
      wrapper.vm.toggleCollect()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({ title: '请先登录', icon: 'none' })
    })

    it('已登录但未收藏应添加收藏', () => {
      wrapper.vm.username = 'testuser'
      wrapper.vm.id = 1
      wrapper.vm.hotel = { id: 1, name: '测试酒店' }
      globalThis.uni.getStorageSync.mockReturnValue([])

      wrapper.vm.toggleCollect()

      expect(wrapper.vm.isCollected).toBe(true)
      expect(globalThis.uni.setStorageSync).toHaveBeenCalled()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({ title: '收藏成功', icon: 'success' })
    })

    it('已收藏应取消收藏', () => {
      wrapper.vm.username = 'testuser'
      wrapper.vm.id = 1
      wrapper.vm.hotel = { id: 1, name: '测试酒店' }
      globalThis.uni.getStorageSync.mockReturnValue([{ id: 1 }])

      wrapper.vm.toggleCollect()

      expect(wrapper.vm.isCollected).toBe(false)
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({ title: '取消收藏', icon: 'none' })
    })
  })

  // ========== 跳转预订 ==========
  describe('goToBook / goToBookWithRoom - 跳转预订', () => {
    beforeEach(() => {
      wrapper.vm.id = 1
      wrapper.vm.hotel = { name: '测试酒店', minPrice: 299, merchantId: 10 }
      wrapper.vm.username = 'testuser'
    })

    it('goToBook 有房型时应提示先选择', () => {
      wrapper.vm.roomTypes = [{ id: 1, typeName: '大床房' }]
      wrapper.vm.goToBook()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '请先选择房型',
        icon: 'none'
      })
    })

    it('selectRoom 应跳转带房型信息', () => {
      const room = { id: 5, typeName: '豪华套房', price: 899 }
      wrapper.vm.hotel = { name: '测试酒店', merchantId: 10 }
      wrapper.vm.selectRoom(room)

      expect(globalThis.uni.navigateTo).toHaveBeenCalled()
      const url = globalThis.uni.navigateTo.mock.calls[0][0].url
      expect(url).toContain('roomTypeId=5')
      expect(url).toContain('roomTypeName=' + encodeURIComponent('豪华套房'))
      expect(url).toContain('price=899')
    })

    it('goToBookWithRoom 应正确生成 URL', () => {
      const room = { id: 3, typeName: '标准间', price: 199 }
      wrapper.vm.goToBookWithRoom(room)

      const url = globalThis.uni.navigateTo.mock.calls[0][0].url
      expect(url).toContain('id=1')
      expect(url).toContain('roomTypeId=3')
      expect(url).toContain('price=199')
    })
  })

  // ========== formatTime - 时间格式化 ==========
  describe('formatTime - 时间格式化', () => {
    it('空值应返回"刚刚"', () => {
      expect(wrapper.vm.formatTime('')).toBe('刚刚')
      expect(wrapper.vm.formatTime(null)).toBe('刚刚')
      expect(wrapper.vm.formatTime(undefined)).toBe('刚刚')
    })

    it('有效日期应格式化为 YYYY-MM-DD', () => {
      expect(wrapper.vm.formatTime('2024-03-15T10:30:00')).toBe('2024-03-15')
    })

    it('无效日期应返回 NaN-NaN-NaN', () => {
      expect(wrapper.vm.formatTime('invalid-date')).toBe('NaN-NaN-NaN')
    })
  })
})
