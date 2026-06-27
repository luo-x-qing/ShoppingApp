import { describe, it, expect, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import SearchFlight from '../../../pages/flight/search-flight.vue'

function createWrapper() {
  return shallowMount(SearchFlight, {
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

describe('search-flight.vue', () => {
  describe('初始数据', () => {
    it('默认搜索参数是北京→上海', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.searchParams.fromCity).toBe('PEK')
      expect(wrapper.vm.searchParams.fromCityName).toBe('北京')
      expect(wrapper.vm.searchParams.toCity).toBe('SHA')
      expect(wrapper.vm.searchParams.toCityName).toBe('上海')
    })

    it('初始加载时设置7天后的日期', () => {
      const wrapper = createWrapper()
      wrapper.vm.$options.onLoad?.call(wrapper.vm)
      const date = new Date()
      date.setDate(date.getDate() + 7)
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      expect(wrapper.vm.searchParams.fromDate).toBe(`${y}-${m}-${d}`)
    })
  })

  describe('swapCities()', () => {
    it('交换出发和到达城市', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromCity = 'PEK'
      wrapper.vm.searchParams.fromCityName = '北京'
      wrapper.vm.searchParams.toCity = 'SHA'
      wrapper.vm.searchParams.toCityName = '上海'
      wrapper.vm.swapCities()
      expect(wrapper.vm.searchParams.fromCity).toBe('SHA')
      expect(wrapper.vm.searchParams.fromCityName).toBe('上海')
      expect(wrapper.vm.searchParams.toCity).toBe('PEK')
      expect(wrapper.vm.searchParams.toCityName).toBe('北京')
    })
  })

  describe('showCityPicker() / closeCityPicker()', () => {
    it('打开出发城市选择器', () => {
      const wrapper = createWrapper()
      wrapper.vm.showCityPicker('from')
      expect(wrapper.vm.cityPickerType).toBe('from')
      expect(wrapper.vm.showCityPickerFlag).toBe(true)
      expect(wrapper.vm.citySearchKeyword).toBe('')
    })

    it('打开到达城市选择器', () => {
      const wrapper = createWrapper()
      wrapper.vm.showCityPicker('to')
      expect(wrapper.vm.cityPickerType).toBe('to')
      expect(wrapper.vm.showCityPickerFlag).toBe(true)
    })

    it('关闭城市选择器时清空搜索关键词', () => {
      const wrapper = createWrapper()
      wrapper.vm.showCityPickerFlag = true
      wrapper.vm.citySearchKeyword = 'beijing'
      wrapper.vm.closeCityPicker()
      expect(wrapper.vm.showCityPickerFlag).toBe(false)
      expect(wrapper.vm.citySearchKeyword).toBe('')
    })
  })

  describe('selectCity()', () => {
    it('选择出发城市', () => {
      const wrapper = createWrapper()
      wrapper.vm.showCityPicker('from')
      wrapper.vm.selectCity({ name: '广州', code: 'CAN' })
      expect(wrapper.vm.searchParams.fromCity).toBe('CAN')
      expect(wrapper.vm.searchParams.fromCityName).toBe('广州')
      expect(wrapper.vm.showCityPickerFlag).toBe(false)
    })

    it('选择到达城市', () => {
      const wrapper = createWrapper()
      wrapper.vm.showCityPicker('to')
      wrapper.vm.selectCity({ name: '深圳', code: 'SZX' })
      expect(wrapper.vm.searchParams.toCity).toBe('SZX')
      expect(wrapper.vm.searchParams.toCityName).toBe('深圳')
      expect(wrapper.vm.showCityPickerFlag).toBe(false)
    })
  })

  describe('onDateChange()', () => {
    it('更新出发日期', () => {
      const wrapper = createWrapper()
      wrapper.vm.onDateChange({ detail: { value: '2026-07-15' } })
      expect(wrapper.vm.searchParams.fromDate).toBe('2026-07-15')
    })
  })

  describe('formatTimeDisplay()', () => {
    it('从 "YYYY-MM-DD HH:mm" 格式提取时间', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatTimeDisplay('2026-06-15 08:30')).toBe('08:30')
    })

    it('从 ISO 格式提取时间', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatTimeDisplay('2026-06-15T14:30:00')).toBe('14:30')
    })

    it('对于空值返回 "--"', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatTimeDisplay(null)).toBe('--')
      expect(wrapper.vm.formatTimeDisplay('')).toBe('--')
    })
  })

  describe('calculateDurationFromMinutes()', () => {
    it('将分钟转换为中文时长', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.calculateDurationFromMinutes(150)).toBe('2小时30分')
      expect(wrapper.vm.calculateDurationFromMinutes(60)).toBe('1小时0分')
      expect(wrapper.vm.calculateDurationFromMinutes(45)).toBe('45分')
    })

    it('对于无效输入返回 "--"', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.calculateDurationFromMinutes(null)).toBe('--')
      expect(wrapper.vm.calculateDurationFromMinutes('')).toBe('--')
      expect(wrapper.vm.calculateDurationFromMinutes('abc')).toBe('--')
    })
  })

  describe('filteredCityList 计算属性', () => {
    it('按首字母分组所有城市', () => {
      const wrapper = createWrapper()
      wrapper.vm.citySearchKeyword = ''
      const result = wrapper.vm.filteredCityList
      expect(result['B']).toBeDefined()
      expect(result['S']).toBeDefined()
      expect(result['G']).toBeDefined()
      expect(result['B'].length).toBeGreaterThanOrEqual(4)
    })

    it('根据关键词过滤城市', () => {
      const wrapper = createWrapper()
      wrapper.vm.citySearchKeyword = '北京'
      const result = wrapper.vm.filteredCityList
      const allCities = Object.values(result).flat()
      expect(allCities.length).toBeGreaterThanOrEqual(2)
      expect(allCities.some(c => c.name.includes('北京'))).toBe(true)
    })

    it('支持拼音搜索', () => {
      const wrapper = createWrapper()
      wrapper.vm.citySearchKeyword = 'beijing'
      const result = wrapper.vm.filteredCityList
      const allCities = Object.values(result).flat()
      expect(allCities.some(c => c.pinyin.includes('beijing'))).toBe(true)
    })
  })

  describe('getMockFlights()', () => {
    it('为北京→上海返回5个航班', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromCityName = '北京'
      wrapper.vm.searchParams.toCityName = '上海'
      wrapper.vm.searchParams.fromDate = '2026-07-15'
      const flights = wrapper.vm.getMockFlights()
      expect(flights.length).toBe(5)
      expect(flights[0].flightNumber).toBe('CA1234')
      expect(flights[0].airline).toBe('中国国航')
      expect(flights[0].price).toBeGreaterThan(0)
    })

    it('未知路线使用默认价格', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromCityName = '未知'
      wrapper.vm.searchParams.toCityName = '未知'
      wrapper.vm.searchParams.fromDate = '2026-07-15'
      const flights = wrapper.vm.getMockFlights()
      expect(flights.length).toBe(5)
    })
  })

  describe('searchFlights()', () => {
    it('出发城市为空时显示提示并返回', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromCity = ''
      wrapper.vm.searchFlights()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请选择出发城市', icon: 'none' })
    })

    it('到达城市为空时显示提示并返回', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.toCity = ''
      wrapper.vm.searchFlights()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请选择到达城市', icon: 'none' })
    })

    it('出发和到达相同时显示提示并返回', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromCity = 'PEK'
      wrapper.vm.searchParams.toCity = 'PEK'
      wrapper.vm.searchFlights()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '出发城市和到达城市不能相同', icon: 'none' })
    })

    it('日期为空时显示提示并返回', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromDate = ''
      wrapper.vm.searchFlights()
      expect(uni.showToast).toHaveBeenCalledWith({ title: '请选择出发日期', icon: 'none' })
    })

    it('API 调用成功后使用返回的航班数据（code=200 格式）', () => {
      const flights = [
        { flightNumber: 'CA1234', airline: '中国国航', price: 850 }
      ]
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromDate = '2026-07-15'
      uni.request.mockImplementation(({ success, complete }) => {
        success({ data: { code: 200, data: flights } })
        complete?.()
      })
      wrapper.vm.searchFlights()
      expect(wrapper.vm.flightList).toEqual(flights)
      expect(wrapper.vm.showResultFlag).toBe(true)
      expect(wrapper.vm.isLoading).toBe(false)
    })

    it('API 调用成功后使用返回的航班数据（success 格式）', () => {
      const flights = [
        { flightNumber: 'MU5678', airline: '东方航空', price: 720 }
      ]
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromDate = '2026-07-15'
      uni.request.mockImplementation(({ success, complete }) => {
        success({ data: { success: true, data: flights } })
        complete?.()
      })
      wrapper.vm.searchFlights()
      expect(wrapper.vm.flightList).toEqual(flights)
      expect(wrapper.vm.showResultFlag).toBe(true)
    })

    it('API 调用失败时使用模拟数据', () => {
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromDate = '2026-07-15'
      uni.request.mockImplementation(({ fail, complete }) => {
        fail(new Error('Network error'))
        complete?.()
      })
      wrapper.vm.searchFlights()
      expect(wrapper.vm.flightList.length).toBe(5)
      expect(wrapper.vm.showResultFlag).toBe(true)
      expect(uni.showToast).toHaveBeenCalledWith({ title: '查询失败，使用演示数据', icon: 'none' })
    })

    it('最多显示20个航班', () => {
      const flights = Array.from({ length: 50 }, (_, i) => ({
        flightNumber: `CA${1000 + i}`, price: 500 + i
      }))
      const wrapper = createWrapper()
      wrapper.vm.searchParams.fromDate = '2026-07-15'
      uni.request.mockImplementation(({ success, complete }) => {
        success({ data: { code: 200, data: flights } })
        complete?.()
      })
      wrapper.vm.searchFlights()
      expect(wrapper.vm.flightList.length).toBe(20)
    })
  })

  describe('parseFlightsFromString()', () => {
    it('直接匹配航班对象', () => {
      const wrapper = createWrapper()
      const dataStr = "{'fcategory':'Y','FlightNo':'CA1234','FlightCompany':'中国国航','FlightDeptimePlanDate':'2026-07-15T08:00','FlightArrtimePlanDate':'2026-07-15T10:30','FlightDepAirport':'PEK','FlightArrAirport':'SHA','FlightDuration':'150','FlightState':'计划'}"
      const result = wrapper.vm.parseFlightsFromString(dataStr)
      expect(result.length).toBe(1)
      expect(result[0].flightNumber).toBe('CA1234')
    })
  })

  describe('selectFlight()', () => {
    it('保存航班信息并跳转到订单页', () => {
      const wrapper = createWrapper()
      const flight = { flightNumber: 'CA1234', price: 850 }
      wrapper.vm.showResultFlag = true
      wrapper.vm.selectFlight(flight)
      expect(uni.setStorageSync).toHaveBeenCalledWith('selectedFlight', flight)
      expect(wrapper.vm.showResultFlag).toBe(false)
      expect(uni.navigateTo).toHaveBeenCalledWith({ url: '/pages/flight/order' })
    })
  })

  describe('closeResult()', () => {
    it('关闭搜索结果弹窗', () => {
      const wrapper = createWrapper()
      wrapper.vm.showResultFlag = true
      wrapper.vm.closeResult()
      expect(wrapper.vm.showResultFlag).toBe(false)
    })
  })
})
