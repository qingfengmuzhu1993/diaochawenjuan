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

    <el-tabs v-model="activeTab" style="margin-top:20px">
      <el-tab-pane label="题目分析" name="questions">
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
      </el-tab-pane>

      <el-tab-pane label="交叉分析" name="cross">
        <div style="display:flex;gap:12px;margin-bottom:16px;align-items:center;flex-wrap:wrap">
          <span style="font-size:13px;color:#5F8B7A">行维度:</span>
          <el-select v-model="crossRowQ" placeholder="选择题目" @change="fetchCross" clearable style="width:220px">
            <el-option v-for="q in crossQuestions" :key="q.questionId" :label="truncateText(q.content, 25)" :value="q.questionId" />
          </el-select>
          <span style="font-size:13px;color:#5F8B7A">列维度:</span>
          <el-select v-model="crossColQ" placeholder="选择题目" @change="fetchCross" clearable style="width:220px">
            <el-option v-for="q in crossQuestions" :key="q.questionId" :label="truncateText(q.content, 25)" :value="q.questionId" />
          </el-select>
        </div>

        <div v-if="crossData">
          <el-tag v-if="crossData.highlySignificant" type="danger" size="large" effect="dark">p &lt; 0.01 极显著</el-tag>
          <el-tag v-else-if="crossData.significant" type="warning" size="large" effect="dark">p &lt; 0.05 显著</el-tag>
          <el-tag v-else type="info" size="large">不显著 (p = {{ crossData.pValue }})</el-tag>

          <span style="margin-left:12px;font-size:12px;color:#999">χ² = {{ crossData.chiSquare }}, df = {{ crossData.degreesOfFreedom }}</span>

          <table class="cross-table" style="margin-top:12px">
            <thead>
              <tr><th></th><th v-for="(cl, ci) in crossData.colLabels" :key="ci">{{ cl }}</th></tr>
            </thead>
            <tbody>
              <tr v-for="(rl, ri) in crossData.rowLabels" :key="ri">
                <td class="cross-row-label">{{ rl }}</td>
                <td v-for="(cl, ci) in crossData.colLabels" :key="ci" class="cross-cell">
                  {{ crossData.matrix[ri][ci] }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <el-empty v-else description="选择两个题目开始交叉分析" />
      </el-tab-pane>
      <el-tab-pane label="智能报告" name="report">
        <div v-if="!reportData" style="text-align:center;padding:40px">
          <el-button type="primary" size="large" @click="generateReport" :loading="reportLoading">
            生成智能报告
          </el-button>
        </div>
        <div v-else class="report-content">
          <h2 style="color:#134E4A;font-size:20px">{{ reportData.surveyTitle }} — 调研报告</h2>
          <p style="color:#999;font-size:13px">样本量：{{ reportData.sampleSize }} | 生成时间：{{ formatDt(reportData.generatedAt) }}</p>
          <el-button type="primary" size="small" @click="downloadPdf" style="margin-bottom:12px">下载PDF报告</el-button>
          <el-alert :title="reportData.summary" type="success" :closable="false" style="margin-bottom:16px" />
          <div v-for="(sec, i) in reportData.sections" :key="i" style="margin-bottom:16px;padding:14px;background:#FAFBFC;border-radius:10px;border:1px solid #E8F5EF">
            <h4 style="color:#0D9488;margin:0 0 6px">{{ sec.title }}</h4>
            <p style="color:#5F8B7A;margin:0;line-height:1.8;font-size:14px">{{ sec.content }}</p>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { analyticsApi } from '@/api/analytics'
import { surveyApi } from '@/api/survey'

const route = useRoute()
const loading = ref(false)
const survey = ref({})
const stats = ref({})
const findings = ref([])
const sentiment = ref({})

const activeTab = ref('questions')
const crossRowQ = ref(null)
const crossColQ = ref(null)
const crossData = ref(null)
const reportData = ref(null)
const reportLoading = ref(false)

const crossQuestions = computed(() => {
  const qa = stats.value.questionAnalysis || []
  return qa.filter(q => q.type === 'single' || q.type === 'judge')
})

function truncateText(text, len) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

async function fetchCross() {
  if (!crossRowQ.value || !crossColQ.value) return
  try {
    crossData.value = await analyticsApi.getCrossTab(route.params.id, crossRowQ.value, crossColQ.value)
  } catch {}
}

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

function formatDt(dt) {
  if (!dt) return ''
  return dt.substring(0, 16).replace('T', ' ')
}

async function generateReport() {
  reportLoading.value = true
  try {
    reportData.value = await analyticsApi.generateReport(route.params.id)
  } catch {} finally { reportLoading.value = false }
}

function downloadPdf() {
  window.open('/api/v1/analytics/surveys/' + route.params.id + '/report/export')
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.finding-item { margin-bottom: 8px; }
.question-stats { margin-bottom: 12px; }
.dist-bar { margin-bottom: 8px; }
.bar-label { display: block; margin-bottom: 2px; font-size: 13px; color: #666; }
.cross-table { width:100%; border-collapse:collapse; }
.cross-table th, .cross-table td { padding:8px 12px; border:1px solid #E8F5EF; text-align:center; font-size:13px; }
.cross-table th { background:#F5FAF8; color:#134E4A; font-weight:600; }
.cross-row-label { text-align:left !important; font-weight:500; color:#134E4A; }
.cross-cell { min-width:50px; }
</style>
