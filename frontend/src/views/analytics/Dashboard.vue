<template>
  <div class="analytics" v-loading="loading">
    <div class="header">
      <h2>数据分析：{{ survey.title }}</h2>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" @click="exportCsv">导出CSV</el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin:20px 0">
      <el-col :span="6"><el-statistic title="回收份数" :value="stats.totalResponses || 0" /></el-col>
      <el-col :span="6"><el-statistic title="目标配额" :value="stats.targetQuota || 0" /></el-col>
      <el-col :span="6"><el-statistic title="平均耗时" :value="stats.avgDurationSeconds || 0" suffix="秒" /></el-col>
    </el-row>

    <h3>关键发现</h3>
    <div v-for="(f, i) in findings" :key="i" class="finding-item">
      <el-alert :title="f.message" :type="f.confidence==='high'?'success':'info'" show-icon :closable="false" />
    </div>

    <h3 style="margin-top:20px">题目分析</h3>
    <div v-for="qa in stats.questionAnalysis" :key="qa.questionId" class="question-stats">
      <el-card>
        <template #header>{{ qa.content }}</template>
        <div v-if="qa.distribution">
          <div v-for="(count, label) in qa.distribution" :key="label" class="dist-bar">
            <span class="bar-label">{{ label }}：{{ count }}</span>
            <el-progress :percentage="percentage(count, qa.distribution)" :stroke-width="16" />
          </div>
        </div>
        <div v-else-if="qa.avgRating !== undefined">
          <span>平均评分：</span>
          <el-rate :model-value="Math.round(qa.avgRating)" disabled show-score :max="5" />
        </div>
        <div v-else>
          <span>共 {{ qa.responseCount }} 条回复</span>
        </div>
      </el-card>
    </div>

    <h3 style="margin-top:20px">情感分析</h3>
    <el-row :gutter="16">
      <el-col :span="8"><el-statistic title="正面" :value="sentiment.positive || 0" value-style="color:#67c23a" /></el-col>
      <el-col :span="8"><el-statistic title="中性" :value="sentiment.neutral || 0" value-style="color:#909399" /></el-col>
      <el-col :span="8"><el-statistic title="负面" :value="sentiment.negative || 0" value-style="color:#f56c6c" /></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { analyticsApi } from '@/api/analytics'
import { surveyApi } from '@/api/survey'

const route = useRoute()
const loading = ref(false)
const survey = ref({})
const stats = ref({})
const findings = ref([])
const sentiment = ref({})

onMounted(async () => {
  loading.value = true
  const id = route.params.id
  try {
    survey.value = await surveyApi.getDetail(id)
    stats.value = await analyticsApi.getStatistics(id)
    const fRes = await analyticsApi.getFindings(id)
    findings.value = fRes.findings || []
    const questions = stats.value.questionAnalysis || []
    if (questions.length > 0) {
      const essayQ = questions.find(q => q.type === 'essay' || q.type === 'fill')
      if (essayQ) sentiment.value = await analyticsApi.getSentiment(id, essayQ.questionId)
    }
  } catch {} finally { loading.value = false }
})

function percentage(count, dist) {
  const total = Object.values(dist).reduce((s, v) => s + v, 0)
  return total > 0 ? Math.round((count / total) * 100) : 0
}

function exportCsv() {
  window.open(analyticsApi.exportCsv(route.params.id))
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.finding-item { margin-bottom: 8px; }
.question-stats { margin-bottom: 12px; }
.dist-bar { margin-bottom: 8px; }
.bar-label { display: block; margin-bottom: 2px; font-size: 13px; color: #666; }
</style>
