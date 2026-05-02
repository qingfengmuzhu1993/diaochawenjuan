import request from './request'
export const responseApi = {
  start: (data) => request.post('/responses/start', data),
  submitAnswers: (id, data) => request.post('/responses/' + id + '/answers', data),
  submit: (id) => request.post('/responses/' + id + '/submit'),
  getMyResponses: (page, size) => request.get('/responses/my', { params: { page: page || 1, size: size || 20 } }),
  getDetail: (id) => request.get('/responses/' + id),
}
