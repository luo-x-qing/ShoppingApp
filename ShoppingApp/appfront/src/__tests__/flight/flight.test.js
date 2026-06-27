import { describe, it, expect, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import Flight from '../../../pages/flight/flight.vue'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('flight.vue', () => {
  it('加载时重定向到 search-flight', () => {
    const wrapper = shallowMount(Flight)
    wrapper.vm.$options.onLoad?.call(wrapper.vm)
    expect(uni.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/flight/search-flight' })
    )
  })

  it('重定向失败时显示提示', () => {
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    uni.redirectTo.mockImplementation(({ fail }) => {
      fail(new Error('跳转失败'))
    })
    const wrapper = shallowMount(Flight)
    wrapper.vm.$options.onLoad?.call(wrapper.vm)
    expect(uni.showToast).toHaveBeenCalledWith({
      title: '页面跳转失败',
      icon: 'none'
    })
    errorSpy.mockRestore()
  })
})
