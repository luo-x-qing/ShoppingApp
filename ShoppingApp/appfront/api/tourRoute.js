const BASE_URL = 'http://localhost:8080'

function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: { 'Content-Type': 'application/json' },
      success: (res) => resolve(res.data),
      fail: (err) => reject(err)
    })
  })
}

export const tourRouteApi = {
  list() {
    return request('/tour-route/list')
  },
  getById(id) {
    return request(`/tour-route/${id}`)
  },
  getDetail(id) {
    return request(`/tour-route/${id}/detail`)
  },
  create(data) {
    return request('/tour-route/add', 'POST', data)
  },
  update(id, data) {
    return request(`/tour-route/${id}`, 'PUT', data)
  },
  delete(id) {
    return request(`/tour-route/${id}`, 'DELETE')
  }
}
