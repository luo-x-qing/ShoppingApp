import { describe, it, expect, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import Order from '../../../pages/flight/order.vue'

function createWrapper() {
  return shallowMount(Order, {
    global: {
      stubs: {
        view: { template: '<div><slot /></div>' },
        text: { template: '<span><slot /></span>' },
        picker: { template: '<div><slot /></div>' },
        button: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        input: { template: '<input />' },
        'scroll-view': { template: '<div><slot /></div>' }
      }
    }
  })
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('order.vue', () => {
  describe('初始数据', () => {
    it('默认乘机人数为1', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.orderData.passengerCount).toBe(1)
      expect(wrapper.vm.passengerList.length).toBe(1)
    })

    it('初始 flightInfo 为空对象', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.flightInfo).toEqual({})
    })
  })

  describe('totalPrice 计算属性', () => {
    it('价格 × 人数 = 总价', () => {
      const wrapper = createWrapper()
      wrapper.vm.flightInfo.price = 850
      wrapper.vm.orderData.passengerCount = 3
      expect(wrapper.vm.totalPrice).toBe(2550)
    })

    it('价格为0或无效时返回0', () => {
      const wrapper = createWrapper()
      wrapper.vm.flightInfo.price = undefined
      wrapper.vm.orderData.passengerCount = 2
      expect(wrapper.vm.totalPrice).toBe(0)
    })
  })

  describe('departureCity / arrivalCity 计算属性', () => {
    it('从机场名提取城市名', () => {
      const wrapper = createWrapper()
      wrapper.vm.flightInfo.departureAirport = '北京首都机场'
      wrapper.vm.flightInfo.arrivalAirport = '上海虹桥机场'
      expect(wrapper.vm.departureCity).toBe('北京首都')
      expect(wrapper.vm.arrivalCity).toBe('上海虹桥')
    })

    it('机场名不含"机场"时直接返回', () => {
      const wrapper = createWrapper()
      wrapper.vm.flightInfo.departureAirport = 'Narita'
      expect(wrapper.vm.departureCity).toBe('Narita')
    })
  })

  describe('onPassengerCountChange()', () => {
    it('增加乘机人数时扩充 passengerList', () => {
      const wrapper = createWrapper()
      wrapper.vm.onPassengerCountChange({ detail: { value: 2 } })
      expect(wrapper.vm.orderData.passengerCount).toBe(3)
      expect(wrapper.vm.passengerList.length).toBe(3)
    })

    it('减少乘机人数时截取 passengerList', () => {
      const wrapper = createWrapper()
      wrapper.vm.onPassengerCountChange({ detail: { value: 1 } })
      expect(wrapper.vm.orderData.passengerCount).toBe(2)
      expect(wrapper.vm.passengerList.length).toBe(2)
      wrapper.vm.onPassengerCountChange({ detail: { value: 0 } })
      expect(wrapper.vm.orderData.passengerCount).toBe(1)
      expect(wrapper.vm.passengerList.length).toBe(1)
    })
  })

  describe('formatDisplayDateTime()', () => {
    it('已含空格的格式直接返回', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatDisplayDateTime('2026-06-15 08:00')).toBe('2026-06-15 08:00')
    })

    it('空值返回 "--"', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatDisplayDateTime('')).toBe('--')
      expect(wrapper.vm.formatDisplayDateTime(null)).toBe('--')
    })
  })

  describe('formatDateTimeForSubmit()', () => {
    it('将 "YYYY-MM-DD HH:mm" 转换为 ISO 格式', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatDateTimeForSubmit('2026-06-15 08:00'))
        .toBe('2026-06-15T08:00:00')
    })

    it('已是 ISO 格式则原样返回', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatDateTimeForSubmit('2026-06-15T08:00:00'))
        .toBe('2026-06-15T08:00:00')
    })

    it('空值返回 null', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatDateTimeForSubmit('')).toBe(null)
      expect(wrapper.vm.formatDateTimeForSubmit(null)).toBe(null)
    })
  })

  describe('onLoad()', () => {
    it('从缓存读取航班信息', () => {
      const flight = { flightNumber: 'CA1234', price: 850 }
      uni.getStorageSync.mockImplementation((key) => {
        if (key === 'selectedFlight') return flight
        if (key === 'loginUsername') return 'testuser'
        return undefined
      })
      const wrapper = createWrapper()
      wrapper.vm.$options.onLoad?.call(wrapper.vm)
      expect(wrapper.vm.flightInfo).toEqual(flight)
      expect(wrapper.vm.orderData.loginUsername).toBe('testuser')
    })

    it('缓存无航班时显示提示', () => {
      uni.getStorageSync.mockReturnValue(undefined)
      const wrapper = createWrapper()
      wrapper.vm.$options.onLoad?.call(wrapper.vm)
      expect(uni.showToast).toHaveBeenCalledWith({
        title: '请先选择航班',
        icon: 'none',
        duration: 2000
      })
    })

    it('未登录时提示登录', () => {
      uni.getStorageSync.mockImplementation((key) => {
        if (key === 'selectedFlight') return { flightNumber: 'CA1234' }
        return ''
      })
      const wrapper = createWrapper()
      wrapper.vm.$options.onLoad?.call(wrapper.vm)
      expect(uni.showToast).toHaveBeenCalledWith({
        title: '请先登录',
        icon: 'none',
        duration: 2000
      })
    })
  })

  describe('submitOrder()', () => {
    it('未登录时提示登录', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = ''
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请先登录', icon: 'none' })
    })

    it('姓名不能为空', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.passengerList[0] = { userName: '', idCard: '', userPhone: '' }
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请输入乘机人1的姓名', icon: 'none' })
    })

    it('姓名不能太短', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.passengerList[0] = { userName: '王', idCard: '', userPhone: '' }
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请输入乘机人1的真实姓名', icon: 'none' })
    })

    it('身份证号不能为空', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.passengerList[0] = { userName: '王小明', idCard: '', userPhone: '' }
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请输入乘机人1的身份证号', icon: 'none' })
    })

    it('身份证号格式不正确', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.passengerList[0] = { userName: '王小明', idCard: '12345', userPhone: '' }
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '乘机人1身份证号格式不正确', icon: 'none' })
    })

    it('手机号不能为空', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.passengerList[0] = { userName: '王小明', idCard: '110101199001011234', userPhone: '' }
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请输入乘机人1的联系电话', icon: 'none' })
    })

    it('手机号格式不正确', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.passengerList[0] = { userName: '王小明', idCard: '110101199001011234', userPhone: '12345' }
      await wrapper.vm.submitOrder()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '乘机人1手机号格式不正确', icon: 'none' })
    })

    it('全部校验通过后调用 API 创建订单', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.flightInfo = {
        flightNumber: 'CA1234',
        price: 850,
        departureTime: '2026-07-15 08:00',
        arrivalTime: '2026-07-15 10:30',
        departureAirport: '北京首都机场',
        arrivalAirport: '上海虹桥机场'
      }
      wrapper.vm.passengerList = [{
        userName: '王小明',
        idCard: '110101199001011234',
        userPhone: '13800138000'
      }]

      uni.request.mockImplementation(({ success }) => {
        success({ statusCode: 200, data: { code: 200, message: '成功' } })
      })

      await wrapper.vm.submitOrder()

      expect(uni.request).toHaveBeenCalledTimes(1)
      expect(uni.showToast).toHaveBeenCalledWith({
        title: '成功预订1个订单！',
        icon: 'success'
      })
      expect(uni.removeStorageSync).toHaveBeenCalledWith('selectedFlight')
    })

    it('API 返回失败时累计失败数', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.flightInfo = { flightNumber: 'CA1234', price: 850 }
      wrapper.vm.passengerList = [{
        userName: '王小明',
        idCard: '110101199001011234',
        userPhone: '13800138000'
      }]

      uni.request.mockImplementation(({ success }) => {
        success({ statusCode: 200, data: { code: 500, message: '失败' } })
      })

      await wrapper.vm.submitOrder()

      expect(uni.showToast).toHaveBeenCalledWith({
        title: '订单提交失败，请重试',
        icon: 'none'
      })
    })

    it('多个乘机人全部成功', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.flightInfo = { flightNumber: 'CA1234', price: 850 }
      wrapper.vm.passengerList = [
        { userName: '王小明', idCard: '110101199001011234', userPhone: '13800138000' },
        { userName: '李小华', idCard: '110101199101011235', userPhone: '13900139000' }
      ]

      uni.request.mockImplementation(({ success }) => {
        success({ statusCode: 200, data: { code: 200, message: '成功' } })
      })

      await wrapper.vm.submitOrder()

      expect(uni.request).toHaveBeenCalledTimes(2)
      expect(uni.showToast).toHaveBeenCalledWith({
        title: '成功预订2个订单！',
        icon: 'success'
      })
    })

    it('API 请求异常时捕获错误', async () => {
      const wrapper = createWrapper()
      wrapper.vm.orderData.loginUsername = 'testuser'
      wrapper.vm.flightInfo = { flightNumber: 'CA1234', price: 850 }
      wrapper.vm.passengerList = [{
        userName: '王小明',
        idCard: '110101199001011234',
        userPhone: '13800138000'
      }]

      uni.request.mockImplementation(({ fail }) => {
        fail(new Error('Network error'))
      })

      await wrapper.vm.submitOrder()

      expect(uni.showToast).toHaveBeenCalledWith({
        title: '订单提交失败，请重试',
        icon: 'none'
      })
    })
  })

  describe('createOrder()', () => {
    it('创建单个订单返回 Promise', async () => {
      const wrapper = createWrapper()
      uni.request.mockImplementation(({ success }) => {
        success({ statusCode: 200, data: { code: 200, data: { id: 1 } } })
      })
      const result = await wrapper.vm.createOrder({ flightNumber: 'CA1234' })
      expect(result.success).toBe(true)
      expect(result.data.data.id).toBe(1)
    })

    it('非200状态码返回失败', async () => {
      const wrapper = createWrapper()
      uni.request.mockImplementation(({ success }) => {
        success({ statusCode: 200, data: { code: 500, message: '余额不足' } })
      })
      const result = await wrapper.vm.createOrder({ flightNumber: 'CA1234' })
      expect(result.success).toBe(false)
      expect(result.message).toBe('余额不足')
    })

    it('请求失败时 reject', async () => {
      const wrapper = createWrapper()
      uni.request.mockImplementation(({ fail }) => {
        fail(new Error('Timeout'))
      })
      await expect(wrapper.vm.createOrder({ flightNumber: 'CA1234' })).rejects.toThrow('Timeout')
    })
  })
})
