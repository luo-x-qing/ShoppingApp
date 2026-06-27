import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { resetUniMock } from './uni.mock.js'
import CategoryPage from '../../pages/category/category.vue'

function createWrapper() {
  return shallowMount(CategoryPage, {
    global: {
      mocks: {}
    }
  })
}

describe('CategoryPage (酒店列表页)', () => {
  let wrapper

  beforeEach(() => {
    resetUniMock(globalThis.uni)
    wrapper = createWrapper()
  })

  // ========== 初始状态 ==========
  describe('初始状态', () => {
    it('应正确初始化 categories', () => {
      expect(wrapper.vm.categoryList).toEqual([
        '全部酒店', '度假型酒店', '商务型酒店', '公寓型酒店', '连锁酒店'
      ])
      expect(wrapper.vm.activeTab).toBe(0)
    })

    it('hotelList 初始为空数组', () => {
      expect(wrapper.vm.hotelList).toEqual([])
    })

    it('排序初始无选中', () => {
      expect(wrapper.vm.currentSort).toBe('')
      expect(wrapper.vm.sortOrder).toBe('asc')
    })

    it('搜索建议初始隐藏', () => {
      expect(wrapper.vm.showSuggest).toBe(false)
      expect(wrapper.vm.suggestList).toEqual([])
    })
  })

  // ========== 分类切换 ==========
  describe('changeTab - 分类切换', () => {
    it('切换分类应更新 activeTab 并重新加载数据', () => {
      wrapper.vm.changeTab(2)
      expect(wrapper.vm.activeTab).toBe(2)
      expect(globalThis.uni.request).toHaveBeenCalled()
    })

    it('切换分类后请求 URL 应包含对应分类', () => {
      wrapper.vm.changeTab(1)
      const callUrl = globalThis.uni.request.mock.calls[0][0].url
      expect(callUrl).toContain('/category/度假型酒店')
    })

    it('切换到"全部酒店"应请求基础 URL', () => {
      wrapper.vm.changeTab(0)
      const callUrl = globalThis.uni.request.mock.calls[0][0].url
      expect(callUrl).toContain('/api/hotels')
      expect(callUrl).not.toContain('/category/')
    })
  })

  // ========== 排序功能 ==========
  describe('changeSort - 排序', () => {
    it('首次点击排序应激活并设为 asc', () => {
      wrapper.vm.changeSort('price')
      expect(wrapper.vm.currentSort).toBe('price')
      expect(wrapper.vm.sortOrder).toBe('asc')
    })

    it('重复点击同一排序应切换顺序', () => {
      wrapper.vm.changeSort('price')
      wrapper.vm.changeSort('price')
      expect(wrapper.vm.sortOrder).toBe('desc')
    })

    it('点击不同排序应切换类型并重置为 asc', () => {
      wrapper.vm.changeSort('price')
      wrapper.vm.changeSort('rating')
      expect(wrapper.vm.currentSort).toBe('rating')
      expect(wrapper.vm.sortOrder).toBe('asc')
    })
  })

  // ========== applySort - 排序逻辑 ==========
  describe('applySort - 排序逻辑', () => {
    const testHotels = [
      { name: 'A', price: 300, distanceValue: 5, rating: 4.5 },
      { name: 'B', price: 100, distanceValue: 10, rating: 3.0 },
      { name: 'C', price: 200, distanceValue: 1, rating: 5.0 }
    ]

    it('价格升序排列', () => {
      wrapper.vm.originalHotelList = testHotels
      wrapper.vm.currentSort = 'price'
      wrapper.vm.sortOrder = 'asc'
      wrapper.vm.applySort()
      expect(wrapper.vm.hotelList.map(h => h.name)).toEqual(['B', 'C', 'A'])
    })

    it('价格降序排列', () => {
      wrapper.vm.originalHotelList = testHotels
      wrapper.vm.currentSort = 'price'
      wrapper.vm.sortOrder = 'desc'
      wrapper.vm.applySort()
      expect(wrapper.vm.hotelList.map(h => h.name)).toEqual(['A', 'C', 'B'])
    })

    it('距离升序排列', () => {
      wrapper.vm.originalHotelList = testHotels
      wrapper.vm.currentSort = 'distance'
      wrapper.vm.sortOrder = 'asc'
      wrapper.vm.applySort()
      expect(wrapper.vm.hotelList.map(h => h.name)).toEqual(['C', 'A', 'B'])
    })

    it('评分降序排列', () => {
      wrapper.vm.originalHotelList = testHotels
      wrapper.vm.currentSort = 'rating'
      wrapper.vm.sortOrder = 'desc'
      wrapper.vm.applySort()
      expect(wrapper.vm.hotelList.map(h => h.name)).toEqual(['C', 'A', 'B'])
    })

    it('无排序条件时按原顺序', () => {
      wrapper.vm.originalHotelList = testHotels
      wrapper.vm.currentSort = ''
      wrapper.vm.applySort()
      expect(wrapper.vm.hotelList.map(h => h.name)).toEqual(['A', 'B', 'C'])
    })

    it('null distanceValue 应排在末尾 (asc)', () => {
      const hotelsWithNull = [
        { name: 'A', price: 100, distanceValue: null, rating: 4 },
        { name: 'B', price: 200, distanceValue: 3, rating: 3 }
      ]
      wrapper.vm.originalHotelList = hotelsWithNull
      wrapper.vm.currentSort = 'distance'
      wrapper.vm.sortOrder = 'asc'
      wrapper.vm.applySort()
      expect(wrapper.vm.hotelList.map(h => h.name)).toEqual(['B', 'A'])
    })
  })

  // ========== 距离计算 ==========
  describe('距离计算', () => {
    it('calculateDistanceValue 应计算正确距离', () => {
      wrapper.vm.currentLocation = { lat: 26.0822, lng: 119.2955 }
      const dist = wrapper.vm.calculateDistanceValue(26.0822, 119.2955)
      expect(dist).toBeCloseTo(0, 1)
    })

    it('formatDistance 应格式化公里数', () => {
      expect(wrapper.vm.formatDistance(1.5)).toBe('1.5km')
      expect(wrapper.vm.formatDistance(0.5)).toBe('500m')
      expect(wrapper.vm.formatDistance(0.05)).toBe('50m')
      expect(wrapper.vm.formatDistance(null)).toBeNull()
    })

    it('toRad 应正确转换角度', () => {
      expect(wrapper.vm.toRad(180)).toBeCloseTo(Math.PI, 5)
      expect(wrapper.vm.toRad(0)).toBe(0)
    })
  })

  // ========== 图片 URL 处理 ==========
  describe('getImageUrl - 图片处理', () => {
    it('空路径应返回默认图片', () => {
      expect(wrapper.vm.getImageUrl('')).toBe('/static/default-hotel.png')
      expect(wrapper.vm.getImageUrl(null)).toBe('/static/default-hotel.png')
      expect(wrapper.vm.getImageUrl(undefined)).toBe('/static/default-hotel.png')
    })

    it('http 路径应直接返回', () => {
      expect(wrapper.vm.getImageUrl('https://example.com/img.jpg'))
        .toBe('https://example.com/img.jpg')
    })

    it('/file 路径应拼接 baseURL', () => {
      expect(wrapper.vm.getImageUrl('/file/hotel/1.jpg'))
        .toBe('http://localhost:8080/file/hotel/1.jpg')
    })

    it('/uploads 路径应拼接 baseURL', () => {
      expect(wrapper.vm.getImageUrl('/uploads/cover.jpg'))
        .toBe('http://localhost:8080/uploads/cover.jpg')
    })
  })

  // ========== 搜索建议 ==========
  describe('onSearchInput - 搜索建议', () => {
    it('空关键词应清除建议', () => {
      wrapper.vm.keyword = ''
      wrapper.vm.onSearchInput({ detail: { value: '' } })
      expect(wrapper.vm.suggestList).toEqual([])
      expect(wrapper.vm.showSuggest).toBe(false)
    })

    it('非空关键词应发起搜索请求', () => {
      wrapper.vm.onSearchInput({ detail: { value: '酒店' } })
      expect(wrapper.vm.keyword).toBe('酒店')
      expect(globalThis.uni.request).toHaveBeenCalled()
      const call = globalThis.uni.request.mock.calls[0][0]
      expect(call.url).toContain('/api/hotels/search')
      expect(call.data.keyword).toBe('酒店')
    })

    it('搜索建议应最多返回 5 条', () => {
      const resp = {
        data: {
          code: 200,
          data: Array.from({ length: 10 }, (_, i) => ({
            id: i + 1,
            name: `酒店${i + 1}`,
            category: '商务型酒店',
            price: 200 + i * 10,
            status: '营业中'
          }))
        }
      }

      wrapper.vm.onSearchInput({ detail: { value: '酒' } })
      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb(resp)

      expect(wrapper.vm.suggestList.length).toBeLessThanOrEqual(5)
      expect(wrapper.vm.showSuggest).toBe(true)
    })

    it('应过滤掉已停业的酒店', () => {
      const resp = {
        data: {
          code: 200,
          data: [
            { id: 1, name: '酒店A', status: '营业中', price: 200, category: '商务型酒店' },
            { id: 2, name: '酒店B', status: '已停业', price: 300, category: '度假型酒店' },
            { id: 3, name: '酒店C', status: '营业中', price: 150, category: '连锁酒店' }
          ]
        }
      }
      wrapper.vm.onSearchInput({ detail: { value: '酒' } })
      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb(resp)

      expect(wrapper.vm.suggestList.map(s => s.id)).toEqual([1, 3])
    })
  })

  // ========== 搜索确认 ==========
  describe('onSearchConfirm - 确认搜索', () => {
    it('有建议列表时应跳转到第一个结果', () => {
      wrapper.vm.suggestList = [
        { id: 1, name: '最佳酒店' },
        { id: 2, name: '次选酒店' }
      ]
      wrapper.vm.keyword = '酒店'
      wrapper.vm.onSearchConfirm()
      expect(globalThis.uni.navigateTo).toHaveBeenCalledWith({
        url: '/pages/hotelDetail/hotelDetail?id=1'
      })
      expect(wrapper.vm.showSuggest).toBe(false)
    })

    it('无建议时应显示提示', () => {
      wrapper.vm.suggestList = []
      wrapper.vm.keyword = '不存在的酒店'
      wrapper.vm.onSearchConfirm()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '未找到相关酒店',
        icon: 'none'
      })
    })

    it('空关键词不应操作', () => {
      wrapper.vm.keyword = ''
      wrapper.vm.onSearchConfirm()
      expect(globalThis.uni.navigateTo).not.toHaveBeenCalled()
    })
  })

  // ========== 加载数据 ==========
  describe('loadData - 数据加载', () => {
    it('应过滤掉已停业的酒店', () => {
      const resp = {
        data: {
          code: 200,
          data: [
            { id: 1, name: '酒店A', status: '营业中', price: 200, starLevel: 4, category: '商务型酒店', avgRating: 4.5 },
            { id: 2, name: '酒店B', status: '已停业', price: 300, starLevel: 5, category: '度假型酒店', avgRating: 3.0 }
          ]
        }
      }
      wrapper.vm.loadData()
      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb(resp)

      expect(wrapper.vm.originalHotelList.length).toBe(1)
      expect(wrapper.vm.originalHotelList[0].name).toBe('酒店A')
    })

    it('请求失败应显示提示', () => {
      wrapper.vm.loadData()
      const failCb = globalThis.uni.request.mock.calls[0][0].fail
      failCb(new Error('Network error'))

      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '加载失败',
        icon: 'none'
      })
    })

    it('加载完成应关闭 loading', () => {
      wrapper.vm.loadData()
      const completeCb = globalThis.uni.request.mock.calls[0][0].complete
      completeCb()
      expect(wrapper.vm.loading).toBe(false)
    })
  })

  // ========== 跳转详情 ==========
  describe('goHotelDetail - 跳转', () => {
    it('应导航到正确详情页', () => {
      wrapper.vm.goHotelDetail(42, '测试酒店')
      expect(globalThis.uni.navigateTo).toHaveBeenCalledWith({
        url: '/pages/hotelDetail/hotelDetail?id=42'
      })
      expect(wrapper.vm.showSuggest).toBe(false)
    })
  })

  // ========== 位置选择器 ==========
  describe('位置选择器', () => {
    it('openLocationPicker 应显示弹窗', () => {
      wrapper.vm.openLocationPicker()
      expect(wrapper.vm.showLocationPicker).toBe(true)
    })

    it('closeLocationPicker 应关闭弹窗', () => {
      wrapper.vm.showLocationPicker = true
      wrapper.vm.closeLocationPicker()
      expect(wrapper.vm.showLocationPicker).toBe(false)
    })

    it('confirmLocation 无选择时应提示', () => {
      wrapper.vm.selectedLocation = null
      wrapper.vm.confirmLocation()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '请先在地图上选择位置',
        icon: 'none'
      })
    })

    it('confirmLocation 有选择时应更新位置', () => {
      wrapper.vm.selectedLocation = {
        address: '测试地址',
        lat: 26.1,
        lng: 119.3
      }
      wrapper.vm.currentLocation = { name: '', lat: null, lng: null }
      wrapper.vm.confirmLocation()

      expect(wrapper.vm.currentLocation).toEqual({
        name: '测试地址',
        lat: 26.1,
        lng: 119.3
      })
      expect(wrapper.vm.showLocationPicker).toBe(false)
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '位置已选择',
        icon: 'success'
      })
    })
  })

  // ========== scrollHeight 计算 ==========
  describe('calcScrollHeight - 滚动高度', () => {
    it('应基于屏幕高度计算', () => {
      wrapper.vm.calcScrollHeight()
      expect(wrapper.vm.scrollHeight).toBeGreaterThan(0)
      expect(wrapper.vm.scrollHeight).toBeLessThan(667)
    })
  })
})
