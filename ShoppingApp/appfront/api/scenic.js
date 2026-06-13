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
  getById(id) {
    return request(`/scenic/${id}`)
  },
  create(data) {
    return request('/scenic/add', 'POST', data)
  },
  update(id, data) {
    return request(`/scenic/${id}`, 'PUT', data)
  },
  delete(id) {
    return request(`/scenic/${id}`, 'DELETE')
  },
  search(province, city, name) {
    let params = []
    if (province) params.push(`province=${encodeURIComponent(province)}`)
    if (city) params.push(`city=${encodeURIComponent(city)}`)
    if (name) params.push(`name=${encodeURIComponent(name)}`)
    return request(`/scenic/search?${params.join('&')}`)
  },
  audit(id, auditStatus) {
    return request(`/scenic/${id}/audit`, 'PUT', { auditStatus })
  },
  auditList(auditStatus = 0) {
    return request(`/scenic/audit/list?auditStatus=${auditStatus}`)
  }
}
