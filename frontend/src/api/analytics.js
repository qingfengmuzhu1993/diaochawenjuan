import request from './request'
export const analyticsApi = {
  getStatistics: (surveyId) => request.get('/analytics/surveys/' + surveyId),
  getFindings: (surveyId) => request.get('/analytics/surveys/' + surveyId + '/findings'),
  getSentiment: (surveyId, questionId) => request.get('/analytics/surveys/' + surveyId + '/sentiment/' + questionId),
  getCrossTab(surveyId, rowQuestionId, colQuestionId) {
    return request.get('/analytics/surveys/' + surveyId + '/cross', {
      params: { rowQuestionId, colQuestionId }
    })
  },
  exportCsv: (surveyId) => '/api/v1/analytics/surveys/' + surveyId + '/export/csv',
  generateReport: (surveyId) => request.post('/analytics/surveys/' + surveyId + '/report'),
}
