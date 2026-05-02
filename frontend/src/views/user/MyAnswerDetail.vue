<template>
  <div class="answer-detail" v-loading="loading">
    <div class="header">
      <h1>{{ detail.surveyTitle }}</h1>
      <div>
        <el-tag v-if="detail.status === 'approved'" type="success" size="large">审核通过</el-tag>
        <el-tag v-else-if="detail.status === 'submitted'" type="warning" size="large">审核中</el-tag>
        <el-tag v-else-if="detail.status === 'rejected'" type="danger" size="large">未通过</el-tag>
        <el-tag v-else size="large">{{ detail.status }}</el-tag>
      </div>
    </div>
    <div class="reward-info" v-if="detail.rewardAmount > 0">
      <span>获得奖励：</span><strong style="color:#F59E0B">¥{{ detail.rewardAmount }}</strong>
      <span v-if="detail.qualityScore" style="margin-left:16px">质量评分：{{ detail.qualityScore }}</span>
    </div>

    <div v-for="(q, idx) in detail.questions" :key="q.questionId" class="answer-card">
      <div class="q-header">
        <span class="q-num">{{ idx + 1 }}.</span>
        <span>{{ q.content }}</span>
        <el-tag v-if="q.required === 1" type="danger" size="small">必答</el-tag>
        <el-tag size="small" type="info" style="margin-left:4px">{{ typeLabel(q.type) }}</el-tag>
      </div>

      <!-- single/multiple/judge -->
      <div v-if="['single','multiple','judge'].includes(q.type)" class="q-answer">
        <div v-for="opt in getSelectedOptions(q)" :key="opt.id" class="q-opt selected">
          <el-tag type="success" size="small">{{ opt.text }}</el-tag>
        </div>
        <div v-if="getSelectedOptions(q).length === 0" class="q-empty">未作答</div>
      </div>

      <!-- rating -->
      <div v-else-if="q.type === 'rating'" class="q-answer">
        <el-rate v-model="q.answerRating" disabled show-score :max="5" />
      </div>

      <!-- essay / fill -->
      <div v-else-if="q.type === 'essay' || q.type === 'fill'" class="q-answer">
        <div class="q-text">{{ q.answerText || '未作答' }}</div>
      </div>

      <!-- matrix -->
      <div v-else-if="q.type === 'matrix'" class="q-answer">
        <div v-if="q.answerOptions" class="q-text">
          {{ formatMatrixAnswer(q.answerOptions, q.options) }}
        </div>
        <div v-else class="q-empty">未作答</div>
      </div>

      <!-- ranking -->
      <div v-else-if="q.type === 'ranking'" class="q-answer">
        <div v-if="q.answerOptions" class="ranking-result">
          <div v-for="(opt, i) in getRankedOptions(q)" :key="i" class="rank-item">
            <span class="rank-num">{{ i + 1 }}</span>
            <span>{{ opt.text }}</span>
          </div>
        </div>
        <div v-else class="q-empty">未作答</div>
      </div>
    </div>

    <div style="margin-top:24px;text-align:center">
      <el-button @click="$router.back()">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { responseApi } from '@/api/response'

const route = useRoute()
const loading = ref(true)
const detail = ref({ questions: [] })

onMounted(async () => {
  try {
    detail.value = await responseApi.getDetail(route.params.responseId)
  } catch {} finally { loading.value = false }
})

function typeLabel(type) {
  const map = { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '简答', rating: '评分', matrix: '矩阵', ranking: '排序' }
  return map[type] || type
}

function parseJson(raw) {
  if (!raw) return []
  try { return typeof raw === 'string' ? JSON.parse(raw) : raw } catch { return [] }
}

function getSelectedOptions(q) {
  const options = parseJson(q.options)
  const answerOpts = parseJson(q.answerOptions)
  if (!Array.isArray(answerOpts) || answerOpts.length === 0) return []
  // For matrix, answerOptions is [{row, col}, ...]; for single/multiple it's [id, ...]
  if (answerOpts.length > 0 && typeof answerOpts[0] === 'object') {
    return [] // matrix handled separately
  }
  return options.filter(o => answerOpts.includes(o.id))
}

function getRankedOptions(q) {
  const options = parseJson(q.options)
  const order = parseJson(q.answerOptions)
  if (!Array.isArray(order) || order.length === 0) return options
  const sorted = [...options].sort((a, b) => {
    const ai = order.indexOf(a.id), bi = order.indexOf(b.id)
    return (ai === -1 ? 999 : ai) - (bi === -1 ? 999 : bi)
  })
  return sorted
}

function formatMatrixAnswer(answerOpts, optionsRaw) {
  const opts = parseJson(answerOpts)
  const qOpts = parseJson(optionsRaw)
  if (!Array.isArray(opts)) return ''
  return opts.map(o => {
    const row = (qOpts?.rows || []).find(r => r.id === o.row)
    const col = (qOpts?.cols || []).find(c => c.id === o.col)
    return (row?.text || '?') + ' → ' + (col?.text || '?')
  }).join('，')
}
</script>

<style scoped>
.answer-detail { max-width: 720px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.header h1 { margin: 0; font-size: 22px; color: #134E4A; }
.reward-info { font-size: 14px; color: #5F8B7A; margin-bottom: 20px; }
.answer-card { background: #FFFFFF; border-radius: 12px; padding: 18px; margin-bottom: 10px; border: 1px solid #E8F5EF; }
.q-header { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #134E4A; margin-bottom: 10px; }
.q-num { font-weight: 700; color: #0D9488; }
.q-answer { padding-left: 24px; }
.q-opt { display: inline-block; margin-right: 8px; margin-bottom: 4px; }
.q-text { font-size: 14px; color: #134E4A; padding: 8px 12px; background: #F5FAF8; border-radius: 8px; }
.q-empty { font-size: 13px; color: #AAA; }
.ranking-result { display: flex; flex-direction: column; gap: 6px; }
.rank-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #134E4A; }
.rank-num { display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background: #0D9488; color: #fff; font-size: 11px; font-weight: 600; }
</style>
