import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { resetUniMock } from './uni.mock.js'
import MessagePage from '../../pages/message/message.vue'

function createWrapper(options = {}) {
  return shallowMount(MessagePage, {
    global: {
      mocks: {
        $options: { onLoad: () => {}, onShow: () => {}, onHide: () => {}, onUnload: () => {} }
      }
    }
  })
}

function mockRequestResponse(mockFn, response) {
  mockFn.mockImplementation(({ success, fail }) => {
    if (success) success(response)
    return {}
  })
}

describe('MessagePage (消息中心)', () => {
  let wrapper

  beforeEach(() => {
    resetUniMock(globalThis.uni)
    globalThis.uni.getStorageSync.mockImplementation((key) => {
      if (key === 'loginUsername') return 'testuser'
      if (key === 'merchant_messages') return null
      return null
    })
    wrapper = createWrapper()
  })

  afterEach(() => {
    wrapper?.unmount()
    vi.useRealTimers()
  })

  // ========== 初始状态 ==========
  describe('初始状态', () => {
    it('数据应初始化为默认值', () => {
      expect(wrapper.vm.username).toBe('')
      expect(wrapper.vm.aiUnreadCount).toBe(0)
      expect(wrapper.vm.aiLastMessage).toBe('我是AI旅游规划助手，点击开始规划你的行程 ✈️')
      expect(wrapper.vm.hotelOrdersList).toEqual([])
      expect(wrapper.vm.flightOrdersList).toEqual([])
      expect(wrapper.vm.hotelUnreadCount).toBe(0)
      expect(wrapper.vm.flightUnreadCount).toBe(0)
      expect(wrapper.vm.showCategoryModal).toBe(false)
      expect(wrapper.vm.categoryTitle).toBe('')
      expect(wrapper.vm.currentCategoryNotices).toEqual([])
      expect(wrapper.vm.merchantMessages).toEqual([])
      expect(wrapper.vm.showMerchantChat).toBe(false)
      expect(wrapper.vm.currentMerchant).toBeNull()
      expect(wrapper.vm.currentMessages).toEqual([])
      expect(wrapper.vm.merchantInput).toBe('')
      expect(wrapper.vm.unreadTimer).toBeNull()
      expect(wrapper.vm.isLoading).toBe(false)
    })
  })

  // ========== getAvatarColor ==========
  describe('getAvatarColor', () => {
    it('相同名称应返回相同颜色', () => {
      const color1 = wrapper.vm.getAvatarColor('测试酒店')
      const color2 = wrapper.vm.getAvatarColor('测试酒店')
      expect(color1).toBe(color2)
    })

    it('不同名称应可能返回不同颜色', () => {
      const c1 = wrapper.vm.getAvatarColor('A')
      const c2 = wrapper.vm.getAvatarColor('B')
      expect(c1).toBeTruthy()
      expect(c2).toBeTruthy()
      expect(c1).toMatch(/^#[0-9a-f]{6}$|^rgb/)
      expect(c2).toMatch(/^#[0-9a-f]{6}$|^rgb/)
    })
  })

  // ========== formatTime ==========
  describe('formatTime - 时间格式化', () => {
    it('空值应返回"刚刚"', () => {
      expect(wrapper.vm.formatTime('')).toBe('刚刚')
      expect(wrapper.vm.formatTime(null)).toBe('刚刚')
      expect(wrapper.vm.formatTime(undefined)).toBe('刚刚')
    })

    it('1分钟内应返回"刚刚"', () => {
      const now = new Date()
      const d = new Date(now.getTime() - 30000)
      expect(wrapper.vm.formatTime(d.toISOString())).toBe('刚刚')
    })

    it('1小时内应返回"X分钟前"', () => {
      const now = new Date()
      const d = new Date(now.getTime() - 5 * 60000)
      expect(wrapper.vm.formatTime(d.toISOString())).toBe('5分钟前')
    })

    it('1天内应返回"X小时前"', () => {
      const now = new Date()
      const d = new Date(now.getTime() - 3 * 3600000)
      expect(wrapper.vm.formatTime(d.toISOString())).toBe('3小时前')
    })

    it('1周内应返回"X天前"', () => {
      const now = new Date()
      const d = new Date(now.getTime() - 2 * 86400000)
      expect(wrapper.vm.formatTime(d.toISOString())).toBe('2天前')
    })

    it('超过1周应返回"X月X日"', () => {
      const d = new Date(2026, 5, 15)
      expect(wrapper.vm.formatTime(d.toISOString())).toBe('6月15日')
    })
  })

  // ========== 加载通知 ==========
  describe('loadOrderNotices - 加载通知', () => {
    it('未登录时应清空列表', () => {
      globalThis.uni.getStorageSync.mockReturnValue('')
      wrapper.vm.username = ''
      wrapper.vm.loadOrderNotices()
      expect(wrapper.vm.hotelOrdersList).toEqual([])
      expect(wrapper.vm.flightOrdersList).toEqual([])
    })

    it('应在接口返回后分类并排序通知', () => {
      wrapper.vm.username = 'testuser'
      wrapper.vm.loadOrderNotices()
      const successCb = globalThis.uni.request.mock.calls[0][0].success
      successCb({
        data: {
          code: 200,
          data: [
            { id: 1, type: 'HOTEL_ORDER', orderId: 101, title: '酒店预订', content: '您已成功预订酒店A', createTime: '2026-06-27T10:00:00', isRead: false },
            { id: 2, type: 'FLIGHT_ORDER', orderId: 201, title: '机票订购', content: '您已成功订购机票B', createTime: '2026-06-27T09:00:00', isRead: true },
            { id: 3, title: '通知', content: '酒店预订成功通知', createTime: '2026-06-27T08:00:00', isRead: false }
          ]
        }
      })
      expect(wrapper.vm.hotelOrdersList.length).toBe(2)
      expect(wrapper.vm.flightOrdersList.length).toBe(1)
      expect(wrapper.vm.hotelUnreadCount).toBe(2)
      expect(wrapper.vm.flightUnreadCount).toBe(0)
    })

    it('接口失败时应清空列表并回调', () => {
      const callback = vi.fn()
      wrapper.vm.username = 'testuser'
      wrapper.vm.loadOrderNotices(callback)
      const failCb = globalThis.uni.request.mock.calls[0][0].fail
      failCb(new Error('Network error'))
      expect(wrapper.vm.hotelOrdersList).toEqual([])
      expect(wrapper.vm.flightOrdersList).toEqual([])
      expect(callback).toHaveBeenCalled()
    })
  })

  // ========== 通知分类弹窗 ==========
  describe('viewCategoryNotices / closeCategoryModal', () => {
    beforeEach(() => {
      wrapper.vm.hotelOrdersList = [
        { id: 1, orderId: 101, title: '酒店A', preview: '已预订', time: '10:00', read: false },
        { id: 2, orderId: 102, title: '酒店B', preview: '已预订', time: '09:00', read: true }
      ]
      wrapper.vm.flightOrdersList = [
        { id: 3, orderId: 201, title: '机票A', preview: '已订购', time: '11:00', read: false }
      ]
    })

    it('打开酒店通知弹窗应设置正确数据', () => {
      wrapper.vm.viewCategoryNotices('hotel')
      expect(wrapper.vm.showCategoryModal).toBe(true)
      expect(wrapper.vm.categoryTitle).toBe('酒店预订成功')
      expect(wrapper.vm.currentCategoryNotices.length).toBe(2)
      expect(wrapper.vm.currentCategoryType).toBe('hotel')
    })

    it('打开机票通知弹窗应设置正确数据', () => {
      wrapper.vm.viewCategoryNotices('flight')
      expect(wrapper.vm.showCategoryModal).toBe(true)
      expect(wrapper.vm.categoryTitle).toBe('机票订购成功')
      expect(wrapper.vm.currentCategoryNotices.length).toBe(1)
      expect(wrapper.vm.currentCategoryType).toBe('flight')
    })

    it('打开弹窗后未读通知应标记为已读', () => {
      wrapper.vm.hotelUnreadCount = 2
      wrapper.vm.viewCategoryNotices('hotel')
      expect(wrapper.vm.hotelUnreadCount).toBe(0)
      expect(wrapper.vm.hotelOrdersList[0].read).toBe(true)
    })

    it('closeCategoryModal 应关闭弹窗并清空列表', () => {
      wrapper.vm.showCategoryModal = true
      wrapper.vm.currentCategoryNotices = [{ id: 1 }]
      wrapper.vm.closeCategoryModal()
      expect(wrapper.vm.showCategoryModal).toBe(false)
      expect(wrapper.vm.currentCategoryNotices).toEqual([])
    })
  })

  // ========== viewOrderDetail ==========
  describe('viewOrderDetail - 查看订单详情', () => {
    it('酒店通知应跳转到酒店订单详情', () => {
      wrapper.vm.currentCategoryType = 'hotel'
      wrapper.vm.viewOrderDetail({ id: 1, orderId: 101 })
      expect(globalThis.uni.navigateTo).toHaveBeenCalledWith({
        url: '/pages/order/hotel-order-detail?id=101'
      })
    })

    it('机票通知应跳转到机票订单详情', () => {
      wrapper.vm.currentCategoryType = 'flight'
      wrapper.vm.viewOrderDetail({ id: 2, orderId: 201 })
      expect(globalThis.uni.navigateTo).toHaveBeenCalledWith({
        url: '/pages/order/flight-order-detail?id=201'
      })
    })

    it('无 orderId 时应弹窗显示内容', () => {
      wrapper.vm.viewOrderDetail({ id: 3, title: '通知', preview: '内容' })
      expect(globalThis.uni.showModal).toHaveBeenCalledWith({
        title: '通知',
        content: '内容',
        showCancel: false,
        confirmText: '知道了'
      })
    })
  })

  // ========== clearAllUnread ==========
  describe('clearAllUnread - 一键已读', () => {
    it('未登录时应提示', () => {
      wrapper.vm.username = ''
      wrapper.vm.clearAllUnread()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({ title: '请先登录', icon: 'none' })
    })

    it('已登录应发起批量已读请求并重置未读计数', () => {
      wrapper.vm.username = 'testuser'
      wrapper.vm.hotelOrdersList = [{ id: 1, read: false }, { id: 2, read: false }]
      wrapper.vm.flightOrdersList = [{ id: 3, read: false }]
      wrapper.vm.hotelUnreadCount = 2
      wrapper.vm.flightUnreadCount = 1
      wrapper.vm.aiUnreadCount = 3
      wrapper.vm.merchantMessages = [{ id: 1, unreadCount: 2, merchantId: 'm1' }]

      wrapper.vm.clearAllUnread()

      expect(globalThis.uni.request).toHaveBeenCalledWith(
        expect.objectContaining({ url: expect.stringContaining('/read-all'), method: 'PUT' })
      )
      expect(wrapper.vm.hotelUnreadCount).toBe(0)
      expect(wrapper.vm.flightUnreadCount).toBe(0)
      expect(wrapper.vm.aiUnreadCount).toBe(0)
      expect(wrapper.vm.hotelOrdersList.every(n => n.read)).toBe(true)
      expect(wrapper.vm.flightOrdersList.every(n => n.read)).toBe(true)
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({ title: '已全部标为已读', icon: 'success' })
    })
  })

  // ========== 商家消息生成 ==========
  describe('generateMerchantMessages - 商家消息生成', () => {
    it('无订单时商家消息为空', () => {
      wrapper.vm.hotelOrders = []
      wrapper.vm.generateMerchantMessages()
      expect(wrapper.vm.merchantMessages).toEqual([])
    })

    it('应从有效订单生成商家消息', () => {
      wrapper.vm.hotelOrders = [
        { id: 1, hotelId: 100, name: '测试酒店', status: '已确认', username: 'testuser', createTime: '2026-06-27T10:00:00' }
      ]
      wrapper.vm.generateMerchantMessages()
      expect(wrapper.vm.merchantMessages.length).toBe(1)
      expect(wrapper.vm.merchantMessages[0].name).toBe('测试酒店')
      expect(wrapper.vm.merchantMessages[0].id).toBe(100)
      expect(wrapper.vm.merchantMessages[0].lastMessage).toContain('订单已确认')
    })

    it('无效状态订单应被过滤', () => {
      wrapper.vm.hotelOrders = [
        { id: 1, hotelId: 100, name: '酒店A', status: '已取消', username: 'testuser' },
        { id: 2, hotelId: 200, name: '酒店B', status: '已退款', username: 'testuser' }
      ]
      wrapper.vm.generateMerchantMessages()
      expect(wrapper.vm.merchantMessages).toEqual([])
    })
  })

  // ========== 商家消息存储 ==========
  describe('saveMerchantMessages - 商家消息存储', () => {
    it('应保存到 storage', () => {
      wrapper.vm.merchantMessages = [{ id: 100, name: '测试酒店', avatar: '测', avatarType: 'text' }]
      wrapper.vm.saveMerchantMessages(100, [{ role: 'user', content: '你好' }], 0, '你好', '10:00')
      expect(globalThis.uni.setStorageSync).toHaveBeenCalled()
      const call = globalThis.uni.setStorageSync.mock.calls.find(c => c[0] === 'merchant_messages')
      expect(call).toBeTruthy()
      const saved = call[1]
      expect(saved).toBeTruthy()
    })
  })

  // ========== 未读计数 ==========
  describe('countTotalUnread - 未读计数', () => {
    it('有未读时应设置徽章', () => {
      wrapper.vm.aiUnreadCount = 1
      wrapper.vm.hotelUnreadCount = 2
      wrapper.vm.flightUnreadCount = 0
      wrapper.vm.merchantMessages = [{ unreadCount: 3 }]

      wrapper.vm.countTotalUnread()

      expect(globalThis.uni.setTabBarBadge).toHaveBeenCalledWith({
        index: 2,
        text: '6'
      })
    })

    it('超过99时应显示99+', () => {
      wrapper.vm.aiUnreadCount = 100
      wrapper.vm.countTotalUnread()
      expect(globalThis.uni.setTabBarBadge).toHaveBeenCalledWith({
        index: 2,
        text: '99+'
      })
    })

    it('没有未读时应移除徽章', () => {
      wrapper.vm.aiUnreadCount = 0
      wrapper.vm.hotelUnreadCount = 0
      wrapper.vm.flightUnreadCount = 0
      wrapper.vm.merchantMessages = []
      wrapper.vm.countTotalUnread()
      expect(globalThis.uni.removeTabBarBadge).toHaveBeenCalledWith({ index: 2 })
    })
  })

  // ========== goToAIChat ==========
  describe('goToAIChat - 跳转AI聊天', () => {
    it('应导航到AI聊天页并重置未读', () => {
      wrapper.vm.aiUnreadCount = 5
      wrapper.vm.goToAIChat()
      expect(globalThis.uni.navigateTo).toHaveBeenCalledWith({
        url: '/pages/ai-chat/ai-chat'
      })
      expect(wrapper.vm.aiUnreadCount).toBe(0)
    })
  })

  // ========== 商家聊天 ==========
  describe('商家聊天 - goToMerchantChat / closeMerchantChat', () => {
    it('goToMerchantChat 应打开聊天弹窗', () => {
      const merchant = {
        id: 100, merchantId: 'm100', hotelId: 1, orderId: 1,
        name: '测试酒店', avatar: '测', avatarType: 'text',
        messages: [], unreadCount: 2, lastMessage: '你好', lastTime: '10:00',
        userId: 'testuser'
      }
      wrapper.vm.username = 'testuser'
      globalThis.uni.request.mockImplementation(({ success, fail }) => {
        if (success) success({ data: { code: 200, data: [] } })
        return {}
      })
      wrapper.vm.goToMerchantChat(merchant)
      expect(wrapper.vm.showMerchantChat).toBe(true)
      expect(wrapper.vm.currentMerchant.id).toBe(merchant.id)
      expect(wrapper.vm.currentMerchant.name).toBe(merchant.name)
    })

    it('closeMerchantChat 应关闭并重置', () => {
      wrapper.vm.showMerchantChat = true
      wrapper.vm.currentMerchant = { id: 1, messages: [] }
      wrapper.vm.currentMessages = [{ role: 'user', content: 'hi' }]
      wrapper.vm.merchantInput = 'hello'
      wrapper.vm.closeMerchantChat()
      expect(wrapper.vm.showMerchantChat).toBe(false)
      expect(wrapper.vm.currentMerchant).toBeNull()
      expect(wrapper.vm.currentMessages).toEqual([])
      expect(wrapper.vm.merchantInput).toBe('')
    })
  })

  // ========== 快速回复 ==========
  describe('sendQuickReply - 快速回复', () => {
    it('应设置输入并发送消息', () => {
      wrapper.vm.currentMerchant = {
        id: 1, merchantId: 'm1', hotelId: 1, orderId: 1,
        name: '酒店', userId: 'testuser',
        messages: [], unreadCount: 0
      }
      wrapper.vm.currentMessages = []
      const initialLength = wrapper.vm.currentMessages.length
      wrapper.vm.sendQuickReply('好的，谢谢')
      expect(wrapper.vm.currentMessages.length).toBe(initialLength + 1)
      expect(wrapper.vm.currentMessages[initialLength].content).toBe('好的，谢谢')
    })
  })

  // ========== 发送消息 ==========
  describe('sendMerchantMessage - 发送消息', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      wrapper.vm.currentMerchant = {
        id: 100, merchantId: 'm100', hotelId: 1, orderId: 1,
        name: '测试酒店', userId: 'testuser',
        messages: [], unreadCount: 0, lastMessage: '', lastTime: ''
      }
      wrapper.vm.username = 'testuser'
      wrapper.vm.merchantInput = '有优惠吗？'
    })

    it('空输入不应发送', () => {
      wrapper.vm.merchantInput = '   '
      wrapper.vm.sendMerchantMessage()
      expect(globalThis.uni.request).not.toHaveBeenCalled()
    })

    it('应添加用户消息到本地', () => {
      wrapper.vm.sendMerchantMessage()
      const lastMsg = wrapper.vm.currentMessages[wrapper.vm.currentMessages.length - 1]
      expect(lastMsg.role).toBe('user')
      expect(lastMsg.content).toBe('有优惠吗？')
    })

    it('应发送消息到服务器', () => {
      wrapper.vm.sendMerchantMessage()
      expect(globalThis.uni.request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: 'http://localhost:8080/api/messages/send',
          method: 'POST',
          data: expect.objectContaining({ content: '有优惠吗？', senderRole: 'user' })
        })
      )
    })

    it('包含"优惠"关键词应自动回复折扣信息', () => {
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('优惠')
      expect(autoReply.content).toContain('测试酒店')
    })

    it('包含"改期"关键词应自动回复改期相关信息', () => {
      wrapper.vm.merchantInput = '我想改期'
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('改期')
    })

    it('包含"停车"关键词应自动回复停车信息', () => {
      wrapper.vm.merchantInput = '有停车场吗？'
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('停车场')
    })

    it('包含"早餐"关键词应自动回复早餐信息', () => {
      wrapper.vm.merchantInput = '早餐几点'
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('早餐')
    })

    it('包含"谢谢"关键词应自动回复', () => {
      wrapper.vm.merchantInput = '谢谢'
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('不客气')
    })

    it('包含"你好"关键词应自动回复问候', () => {
      wrapper.vm.merchantInput = '你好'
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('您好')
    })

    it('无匹配关键词应回复默认消息', () => {
      wrapper.vm.merchantInput = '随便看看'
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)

      const autoReply = wrapper.vm.currentMessages.find(m => m.role === 'merchant')
      expect(autoReply).toBeTruthy()
      expect(autoReply.content).toContain('感谢您的留言')
    })

    it('无客户信息应提示', () => {
      wrapper.vm.currentMerchant.userId = ''
      wrapper.vm.sendMerchantMessage()
      expect(globalThis.uni.showToast).toHaveBeenCalledWith({ title: '无法获取客户信息', icon: 'none' })
    })

    it('自动回复后应保存到 storage', () => {
      wrapper.vm.sendMerchantMessage()
      vi.advanceTimersByTime(900)
      expect(globalThis.uni.setStorageSync).toHaveBeenCalled()
    })
  })

  // ========== 轮询 ==========
  describe('轮询 - startUnreadPolling / stopUnreadPolling', () => {
    it('startUnreadPolling 应启动定时器', () => {
      vi.useFakeTimers()
      wrapper.vm.refreshUnreadCount = vi.fn()
      wrapper.vm.startUnreadPolling()
      expect(wrapper.vm.unreadTimer).not.toBeNull()
      vi.advanceTimersByTime(30000)
      expect(wrapper.vm.refreshUnreadCount).toHaveBeenCalledTimes(1)
      vi.useRealTimers()
    })

    it('stopUnreadPolling 应清除定时器', () => {
      vi.useFakeTimers()
      wrapper.vm.unreadTimer = setInterval(() => {}, 30000)
      wrapper.vm.stopUnreadPolling()
      expect(wrapper.vm.unreadTimer).toBeNull()
      vi.useRealTimers()
    })
  })

  // ========== refreshUnreadCount ==========
  describe('refreshUnreadCount - 刷新未读', () => {
    it('未登录不应操作', () => {
      wrapper.vm.username = ''
      wrapper.vm.refreshUnreadCount()
      expect(globalThis.uni.request).not.toHaveBeenCalled()
    })
  })

  // ========== markNoticeRead ==========
  describe('markNoticeRead - 标记已读', () => {
    it('应发起 PUT 请求', () => {
      wrapper.vm.markNoticeRead(1)
      expect(globalThis.uni.request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: 'http://localhost:8080/api/notices/1/read',
          method: 'PUT'
        })
      )
    })
  })

  // ========== loadHotelOrders / loadFlightOrders ==========
  describe('loadHotelOrders / loadFlightOrders', () => {
    it('loadHotelOrders 未登录时返回空', () => {
      wrapper.vm.username = ''
      wrapper.vm.loadHotelOrders()
      expect(wrapper.vm.hotelOrders).toEqual([])
    })

    it('loadFlightOrders 未登录时返回空', () => {
      wrapper.vm.username = ''
      wrapper.vm.loadFlightOrders()
    })
  })

  // ========== 生命周期 ==========
  describe('生命周期', () => {
    it('onHide 应停止轮询', () => {
      wrapper.vm.stopUnreadPolling = vi.fn()
      const onHide = wrapper.vm.$options.onHide
      if (onHide) onHide.call(wrapper.vm)
      expect(wrapper.vm.stopUnreadPolling).toHaveBeenCalled()
    })

    it('onUnload 应停止轮询', () => {
      wrapper.vm.stopUnreadPolling = vi.fn()
      const onUnload = wrapper.vm.$options.onUnload
      if (onUnload) onUnload.call(wrapper.vm)
      expect(wrapper.vm.stopUnreadPolling).toHaveBeenCalled()
    })
  })
})
