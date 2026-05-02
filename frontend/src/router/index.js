import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/marketplace',
    children: [
      { path: 'marketplace', name: 'Marketplace', component: () => import('@/views/marketplace/Index.vue') },
      { path: 'marketplace/:id', name: 'MarketplaceDetail', component: () => import('@/views/marketplace/Detail.vue') },
      { path: 'marketplace/:id/answer', name: 'SurveyAnswer', component: () => import('@/views/marketplace/Answer.vue') },
      { path: 'surveys', name: 'MySurveys', component: () => import('@/views/survey/MySurveys.vue') },
      { path: 'surveys/create', name: 'SurveyCreate', component: () => import('@/views/survey/Create.vue') },
      { path: 'surveys/:id/edit', name: 'SurveyEdit', component: () => import('@/views/survey/Edit.vue') },
      { path: 'surveys/:id', name: 'SurveyDetail', component: () => import('@/views/survey/Detail.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/Profile.vue') },
      { path: 'profile/:id', name: 'UserProfile', component: () => import('@/views/user/Profile.vue') },
      { path: 'wallet', name: 'Wallet', component: () => import('@/views/user/Wallet.vue') },
      { path: 'my-answers', name: 'MyAnswers', component: () => import('@/views/user/MyAnswers.vue') },
      { path: 'my-answer/:responseId', name: 'MyAnswerDetail', component: () => import('@/views/user/MyAnswerDetail.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/user/Notifications.vue') },
      { path: 'analytics/:id', name: 'Analytics', component: () => import('@/views/analytics/Dashboard.vue') },
      { path: 'admin/users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
      { path: 'admin/surveys', name: 'AdminSurveys', component: () => import('@/views/admin/Surveys.vue') },
      { path: 'admin/finance', name: 'AdminFinance', component: () => import('@/views/admin/Finance.vue') },
    ],
  },
  { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/Register.vue') },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else {
    next()
  }
})

export default router
