import request from './request'
export const surveyApi = {
  create: (data) => request.post('/surveys', data),
  update: (id, data) => request.put('/surveys/' + id, data),
  getDetail: (id) => request.get('/surveys/' + id),
  getMySurveys: (params) => request.get('/surveys/my', { params }),
  publish: (id, data) => request.post('/surveys/' + id + '/publish', data),
  close: (id) => request.post('/surveys/' + id + '/close'),
  delete: (id) => request.delete('/surveys/' + id),
  aiGenerate: (data) => request.post('/ai/generate-survey', data),
  aiDiagnose: (surveyId) => request.post('/ai/diagnose/' + surveyId),
  getTemplates: (category) => request.get('/templates', { params: { category } }),
  getTemplate: (id) => request.get('/templates/' + id),
  shareSurvey(id) {
    return request.post('/surveys/' + id + '/share')
  },
}
