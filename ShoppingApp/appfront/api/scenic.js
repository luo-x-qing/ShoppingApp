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

export const scenicApi = {
  list() {
    return request('/scenic/list')
  },
  create(data) {
    return request('/scenic/Scenicadd', 'POST', data)
  }
}
