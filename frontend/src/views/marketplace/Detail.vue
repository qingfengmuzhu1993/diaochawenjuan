<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-card">
      <div class="detail-header">
        <h1>{{ survey.title }}</h1>
        <el-tag type="warning" size="large" effect="plain" round>¥{{ survey.rewardPerResponse || 0 }}/份</el-tag>
      </div>
      <p class="detail-desc">{{ survey.description || '暂无描述' }}</p>

      <div class="detail-stats">
        <div class="stat-item">
          <span class="stat-label">题目数量</span>
          <span class="stat-value">{{ survey.totalQuestions || 0 }} 题</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">预计时长</span>
          <span class="stat-value">{{ estimatedTime }} 分钟</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已回收</span>
          <span class="stat-value">{{ survey.totalResponses || 0 }} / {{ survey.targetQuota || '不限' }} 份</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">剩余配额</span>
          <span class="stat-value">{{ survey.remainingQuota || 0 }} 份</span>
        </div>
      </div>

      <div class="creator-info" v-if="survey.userId">
        <span>创建者：<router-link :to="'/profile/' + survey.userId" class="creator-link">用户 #{{ survey.userId }}</router-link></span>
        <el-button v-if="survey.userId && !isSelf" link size="small" type="primary" @click="toggleFollow">
          {{ following ? '已关注' : '+ 关注' }}
        </el-button>
        <span v-if="survey.startTime">开始：{{ formatDate(survey.startTime) }}</span>
        <span v-if="survey.endTime">截止：{{ formatDate(survey.endTime) }}</span>
      </div>
    </div>

    <div v-if="previewQuestions.length > 0" class="preview-section">
      <h3>题目预览（前2题）</h3>
      <div v-for="(q, idx) in previewQuestions" :key="q.id" class="preview-question">
        <div class="preview-q-header">
          <span class="preview-q-num">{{ idx + 1 }}.</span>
          <span>{{ q.content }}</span>
          <el-tag v-if="q.required === 1" type="danger" size="small">必答</el-tag>
          <el-tag size="small" type="info" style="margin-left:4px">{{ typeLabel(q.type) }}</el-tag>
        </div>
        <div class="preview-q-options" v-if="['single','multiple','judge'].includes(q.type)">
          <div v-for="opt in getOptions(q)" :key="opt.id" class="preview-opt">
            <span>{{ opt.text }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="detail-actions">
      <el-button type="primary" size="large" round :disabled="!canClaim" @click="handleClaim" :loading="claiming">
        {{ canClaim ? '立即回答' : '配额已满' }}
      </el-button>
      <el-button size="large" round @click="$router.back()">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { surveyApi } from '@/api/survey'
import { marketplaceApi } from '@/api/marketplace'
import { userApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(true)
const claiming = ref(false)
const survey = ref({})
const following = ref(false)
const isSelf = computed(() => survey.value.userId === auth.user?.id)

const canClaim = computed(() => (survey.value.remainingQuota || 0) > 0)
const previewQuestions = computed(() => (survey.value.questions || []).slice(0, 2))
const estimatedTime = computed(() => {
  const n = survey.value.totalQuestions || 0
  return Math.max(1, Math.round(n * 0.3))
})

onMounted(async () => {
  try {
    survey.value = await surveyApi.getDetail(route.params.id)
    if (survey.value.userId && !isSelf.value) {
      following.value = await userApi.isFollowing(survey.value.userId)
    }
  } catch {} finally { loading.value = false }
})

async function toggleFollow() {
  if (!survey.value.userId) return
  try {
    if (following.value) {
      await userApi.unfollow(survey.value.userId)
      following.value = false
    } else {
      await userApi.follow(survey.value.userId)
      following.value = true
    }
  } catch {}
}

function typeLabel(type) {
  const map = { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '简答', rating: '评分', matrix: '矩阵', ranking: '排序' }
  return map[type] || type
}

function getOptions(q) {
  if (!q.options) return []
  try { return typeof q.options === 'string' ? JSON.parse(q.options) : q.options } catch { return [] }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

async function handleClaim() {
  claiming.value = true
  try {
    const res = await marketplaceApi.claim(route.params.id)
    router.push('/marketplace/' + route.params.id + '/answer?responseId=' + res.responseId)
  } catch {} finally { claiming.value = false }
}
</script>

<style scoped>
.detail-page { max-width: 720px; margin: 0 auto; }
.detail-card { background: var(--app-bg-card); border-radius: 16px; padding: 28px; border: 1px solid var(--app-border-light); box-shadow: var(--shadow-card); margin-bottom: 20px; }
.detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.detail-header h1 { margin: 0; font-size: 22px; color: var(--app-text-primary); line-height: 1.4; flex: 1; margin-right: 16px; }
.detail-desc { color: var(--app-text-secondary); font-size: 14px; line-height: 1.8; margin: 0 0 20px; }
.detail-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-item { text-align: center; padding: 12px; background: var(--app-primary-bg); border-radius: 10px; }
.stat-label { display: block; font-size: 12px; color: var(--app-text-muted); margin-bottom: 4px; }
.stat-value { font-size: 16px; font-weight: 700; color: var(--app-text-primary); }
.creator-info { display: flex; gap: 20px; font-size: 13px; color: var(--app-text-muted); flex-wrap: wrap; align-items: center; }
.creator-link { color: var(--app-primary); text-decoration: none; font-weight: 500; }
.creator-link:hover { text-decoration: underline; }
.preview-section { margin-bottom: 24px; }
.preview-section h3 { color: var(--app-text-primary); font-size: 16px; margin-bottom: 12px; }
.preview-question { background: var(--app-primary-bg); border-radius: 12px; padding: 16px; margin-bottom: 10px; border: 1px solid var(--app-border-light); }
.preview-q-header { display: flex; align-items: center; gap: 6px; font-size: 14px; color: var(--app-text-primary); margin-bottom: 8px; }
.preview-q-num { font-weight: 700; color: var(--app-primary); }
.preview-q-options { padding-left: 24px; }
.preview-opt { padding: 4px 0; font-size: 13px; color: var(--app-text-secondary); }
.detail-actions { display: flex; gap: 12px; justify-content: center; }
</style>
