import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { resetUniMock } from './uni.mock.js'
import BookingPage from '../../pages/booking/booking.vue'

function createWrapper() {
  return shallowMount(BookingPage, {
    global: {}
  })
}

describe('BookingPage (酒店预订页)', () => {
  let wrapper

  beforeEach(() => {
    resetUniMock(globalThis.uni)
    wrapper = createWrapper()
  })

  // ========== 初始状态 ==========
  describe('初始状态', () => {
    it('数据应初始化为默认值', () => {
      expect(wrapper.vm.hotelName).toBe('')
      expect(wrapper.vm.price).toBe(0)
      expect(wrapper.vm.roomCount).toBe(1)
      expect(wrapper.vm.days).toBe(1)
      expect(wrapper.vm.totalPrice).toBe(0)
      expect(wrapper.vm.contactPhone).toBe('')
      expect(wrapper.vm.roomTypeId).toBeNull()
      expect(wrapper.vm.roomTypeName).toBe('')
      expect(wrapper.vm.showRoomPicker).toBe(false)
    })
  })

  // ========== onLoad - 页面加载 ==========
  describe('onLoad - 页面加载', () => {
    function callOnLoad(options) {
      wrapper.vm.$options.onLoad.call(wrapper.vm, options)
    }

    it('应正确解析传入参数', () => {
      callOnLoad({
        id: '5',
        name: encodeURIComponent('测试酒店'),
        price: '299',
        username: 'testuser'
      })

      expect(wrapper.vm.id).toBe('5')
      expect(wrapper.vm.hotelName).toBe('测试酒店')
      expect(wrapper.vm.price).toBe(299)
      expect(wrapper.vm.username).toBe('testuser')
    })

    it('带房型参数时应自动设置选中房型', () => {
      callOnLoad({
        id: '5',
        name: encodeURIComponent('酒店'),
        price: '399',
        roomTypeId: '10',
        roomTypeName: encodeURIComponent('豪华房'),
        username: 'testuser'
      })

      expect(wrapper.vm.roomTypeId).toBe('10')
      expect(wrapper.vm.roomTypeName).toBe('豪华房')
      expect(wrapper.vm.selectedRoom).toEqual({
        id: '10',
        typeName: '豪华房',
        price: 399
      })
    })

    it('应设置默认入住日期为明天', () => {
      const today = new Date()
      const tomorrow = new Date(today)
      tomorrow.setDate(tomorrow.getDate() + 1)
      const expectedDate = wrapper.vm.formatDateStr(tomorrow)

      callOnLoad({ id: '1', name: '酒店', price: '100' })

      expect(wrapper.vm.checkInDate).toBe(expectedDate)
    })

    it('应设置默认退房日期为后天', () => {
      const today = new Date()
      const dayAfter = new Date(today)
      dayAfter.setDate(dayAfter.getDate() + 2)
      const expectedDate = wrapper.vm.formatDateStr(dayAfter)

      callOnLoad({ id: '1', name: '酒店', price: '100' })

      expect(wrapper.vm.checkOutDate).toBe(expectedDate)
    })

    it('应加载房型列表', () => {
      callOnLoad({ id: '1', name: '酒店', price: '100' })
      expect(globalThis.uni.request).toHaveBeenCalled()
      const call = globalThis.uni.request.mock.calls[0][0]
      expect(call.url).toContain('/api/room-types/hotel/1')
    })
  })

  // ========== 日期计算 ==========
  describe('日期计算', () => {
    beforeEach(() => {
      wrapper.vm.price = 200
      wrapper.vm.roomCount = 1
      wrapper.vm.checkInDate = '2024-06-01'
      wrapper.vm.checkOutDate = '2024-06-03'
    })

    it('calcDays 应正确计算天数和总价', () => {
      wrapper.vm.calcDays()
      expect(wrapper.vm.days).toBe(2)
      expect(wrapper.vm.totalPrice).toBe(400)
    })

    it('选择房型后应重新计算', () => {
      const room = { id: 1, typeName: '大床房', price: 300 }
      wrapper.vm.selectRoom(room)

      expect(wrapper.vm.price).toBe(300)
      expect(wrapper.vm.days).toBe(2)
      expect(wrapper.vm.totalPrice).toBe(600)
    })

    it('同一天入住退房应至少算 1 天', () => {
      wrapper.vm.checkInDate = '2024-06-01'
      wrapper.vm.checkOutDate = '2024-06-01'
      wrapper.vm.calcDays()
      expect(wrapper.vm.days).toBe(1)
    })
  })

  // ========== 房间数量 ==========
  describe('房间数量控制', () => {
    it('decreaseCount 不应小于 1', () => {
      wrapper.vm.roomCount = 1
      wrapper.vm.decreaseCount()
      expect(wrapper.vm.roomCount).toBe(1)
    })

    it('decreaseCount 应减少数量', () => {
      wrapper.vm.roomCount = 3
      wrapper.vm.decreaseCount()
      expect(wrapper.vm.roomCount).toBe(2)
    })

    it('increaseCount 应增加数量', () => {
      wrapper.vm.selectedRoom = { availableCount: 10 }
      wrapper.vm.roomCount = 1
      wrapper.vm.increaseCount()
      expect(wrapper.vm.roomCount).toBe(2)
    })

    it('increaseCount 不应超过库存', () => {
      wrapper.vm.selectedRoom = { availableCount: 3 }
      wrapper.vm.roomCount = 3
      wrapper.vm.increaseCount()
      expect(wrapper.vm.roomCount).toBe(3)
      expect(globalThis.uni.showToast).toHaveBeenCalled()
    })

    it('未选房型时 increaseCount 不应受限', () => {
      wrapper.vm.selectedRoom = null
      wrapper.vm.roomCount = 1
      wrapper.vm.increaseCount()
      expect(wrapper.vm.roomCount).toBe(2)
    })
  })

  // ========== 日期验证 ==========
  describe('日期验证', () => {
    beforeEach(() => {
      wrapper.vm.checkInDate = '2024-06-01'
    })

    it('入住日期早于今天应重置', () => {
      const pastDate = '2020-01-01'
      wrapper.vm.bindCheckInChange({ detail: { value: pastDate } })
      expect(wrapper.vm.checkInDate).not.toBe(pastDate)
    })

    it('退房日期不晚于入住日期应重置', () => {
      wrapper.vm.checkInDate = '2024-06-01'
      wrapper.vm.bindCheckOutChange({ detail: { value: '2024-05-30' } })
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '退房日期必须晚于入住日期',
        icon: 'none'
      })
    })

    it('退房日期晚于入住日期应接受', () => {
      wrapper.vm.checkInDate = '2024-06-01'
      wrapper.vm.bindCheckOutChange({ detail: { value: '2024-06-03' } })
      expect(wrapper.vm.checkOutDate).toBe('2024-06-03')
    })

    it('合法入住日期应被接受', () => {
      const futureDate = '2028-06-15'
      wrapper.vm.bindCheckInChange({ detail: { value: futureDate } })
      expect(wrapper.vm.checkInDate).toBe(futureDate)
    })
  })

  // ========== 房型选择 ==========
  describe('房型选择', () => {
    it('selectRoom 应更新选中房型并关闭弹窗', () => {
      const room = { id: 2, typeName: '双床房', price: 450 }
      wrapper.vm.selectRoom(room)

      expect(wrapper.vm.selectedRoom).toEqual(room)
      expect(wrapper.vm.roomTypeId).toBe(2)
      expect(wrapper.vm.roomTypeName).toBe('双床房')
      expect(wrapper.vm.price).toBe(450)
      expect(wrapper.vm.showRoomPicker).toBe(false)
      expect(globalThis.uni.showToast).toHaveBeenCalled()
    })
  })

  // ========== submitOrder - 提交订单 ==========
  describe('submitOrder - 提交订单', () => {
    beforeEach(() => {
      wrapper.vm.id = '1'
      wrapper.vm.hotelName = '测试酒店'
      wrapper.vm.roomTypeId = 5
      wrapper.vm.roomTypeName = '大床房'
      wrapper.vm.roomCount = 2
      wrapper.vm.totalPrice = 800
      wrapper.vm.checkInDate = '2028-06-01'
      wrapper.vm.checkOutDate = '2028-06-03'
      wrapper.vm.username = 'testuser'
      wrapper.vm.contactPhone = '13800138000'
    })

    it('未选房型应提示', () => {
      wrapper.vm.roomTypeId = null
      wrapper.vm.submitOrder()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '请选择房型',
        icon: 'none'
      })
    })

    it('未填电话应提示', () => {
      wrapper.vm.contactPhone = ''
      wrapper.vm.submitOrder()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '请输入联系电话',
        icon: 'none'
      })
    })

    it('电话格式错误应提示', () => {
      wrapper.vm.contactPhone = '12345'
      wrapper.vm.submitOrder()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '手机号格式不正确',
        icon: 'none'
      })
    })

    it('入住日期早于今天应提示', () => {
      wrapper.vm.checkInDate = '2020-01-01'
      wrapper.vm.submitOrder()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '入住日期不能是今天之前',
        icon: 'none'
      })
    })

    it('退房日期不晚于入住日期应提示', () => {
      wrapper.vm.checkInDate = '2028-06-03'
      wrapper.vm.checkOutDate = '2028-06-01'
      wrapper.vm.submitOrder()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '退房日期必须晚于入住日期',
        icon: 'none'
      })
    })

    it('所有验证通过应提交订单', () => {
      wrapper.vm.submitOrder()

      expect(globalThis.uni.showLoading).toHaveBeenCalledWith({ title: '提交中...' })
      expect(globalThis.uni.request).toHaveBeenCalled()
      
      const call = globalThis.uni.request.mock.calls[0][0]
      expect(call.url).toContain('/api/hotel-orders')
      expect(call.method).toBe('POST')
      expect(call.data).toMatchObject({
        hotelId: 1,
        name: '测试酒店',
        roomTypeId: 5,
        roomCount: 2,
        price: 800,
        checkIn: '2028-06-01',
        checkOut: '2028-06-03',
        username: 'testuser',
        contactPhone: '13800138000',
        status: '待支付'
      })
    })

    it('订单提交成功后应跳转', () => {
      wrapper.vm.submitOrder()

      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({ data: { code: 200 } })

      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '预订成功！',
        icon: 'success'
      })
    })

    it('订单提交失败应显示错误信息', () => {
      wrapper.vm.submitOrder()

      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({ data: { code: 400, message: '房间已满' } })

      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '房间已满',
        icon: 'none'
      })
    })

    it('网络错误应提示重试', () => {
      wrapper.vm.submitOrder()

      const failCb = globalThis.uni.request.mock.calls[0][0].fail
      failCb(new Error('Network error'))

      expect(globalThis.uni.showToast).toHaveBeenCalledWith({
        title: '网络错误，请重试',
        icon: 'none'
      })
    })
  })

  // ========== formatDateStr ==========
  describe('formatDateStr', () => {
    it('应正确格式化日期', () => {
      const date = new Date(2024, 0, 15)
      expect(wrapper.vm.formatDateStr(date)).toBe('2024-01-15')
    })

    it('应补零月份和日期', () => {
      const date = new Date(2024, 2, 5)
      expect(wrapper.vm.formatDateStr(date)).toBe('2024-03-05')
    })
  })
})
