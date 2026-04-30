import request from './request'
export const responseApi = {
  start: (data) => request.post('/responses/start', data),
  submitAnswers: (id, data) => request.post('/responses/' + id + '/answers', data),
  submit: (id) => request.post('/responses/' + id + '/submit'),
}
