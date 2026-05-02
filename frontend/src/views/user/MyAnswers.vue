<template>
  <div class="my-answers">
    <div class="hero-section">
      <div class="hero-left">
        <h1 class="page-title">我的回答</h1>
        <p class="hero-subtitle">查看你提交的所有问卷回答和审核结果</p>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" stripe class="styled-table" empty-text="暂无回答记录">
      <el-table-column prop="survey_title" label="问卷标题" min-width="220">
        <template #default="{ row }">
          <router-link :to="'/my-answer/' + row.id" class="survey-link">{{ row.survey_title }}</router-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'approved'" effect="dark" round class="status-ok">审核通过</el-tag>
          <el-tag v-else-if="row.status === 'submitted'" effect="plain" round class="status-pending">审核中</el-tag>
          <el-tag v-else-if="row.status === 'in_progress'" effect="plain" round type="info">未完成</el-tag>
          <el-tag v-else-if="row.status === 'rejected'" effect="dark" round type="danger">未通过</el-tag>
          <el-tag v-else-if="row.status === 'expired'" effect="plain" round type="info">已过期</el-tag>
          <el-tag v-else effect="plain" round size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reward_amount" label="奖励" width="100" align="right">
        <template #default="{ row }">
          <span v-if="row.reward_amount > 0" class="reward-cell">¥{{ row.reward_amount }}</span>
          <span v-else class="none-cell">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="quality_score" label="质量评分" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.quality_score" class="score-cell" :class="scoreClass(row.quality_score)">{{ row.quality_score }}</span>
          <span v-else class="none-cell">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="duration_seconds" label="耗时" width="100" align="center">
        <template #default="{ row }">
          <span class="time-cell">{{ formatDuration(row.duration_seconds) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="提交时间" width="175">
        <template #default="{ row }">
          <span class="time-cell">{{ formatDate(row.created_at) }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { responseApi } from '@/api/response'

const list = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try { list.value = await responseApi.getMyResponses() } catch {} finally { loading.value = false }
})

function formatDuration(seconds) {
  if (!seconds) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? m + '分' + s + '秒' : s + '秒'
}

function formatDate(dt) {
  if (!dt) return ''
  return dt.substring(0, 16).replace('T', ' ')
}

function scoreClass(score) {
  if (score >= 8) return 'score-high'
  if (score >= 6) return 'score-mid'
  return 'score-low'
}
</script>

<style scoped>
/* ====== Hero ====== */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}
.hero-subtitle {
  margin: 8px 0 0;
  font-size: 15px;
  color: var(--app-text-muted);
  font-weight: 400;
}

/* ====== Links ====== */
.survey-link {
  color: var(--app-primary);
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
}
.survey-link:hover { text-decoration: underline; }

/* ====== Cells ====== */
.reward-cell {
  color: #F59E0B;
  font-weight: 700;
  font-size: 14px;
}
.none-cell { color: var(--app-text-muted); }
.time-cell { color: var(--app-text-muted); font-size: 13px; }

.score-cell { font-weight: 700; font-size: 14px; }
.score-high { color: #0D9488; }
.score-mid { color: #F59E0B; }
.score-low { color: #EF4444; }

/* ====== Status Tags ====== */
.status-ok { background: #0D9488 !important; border-color: #0D9488 !important; }
.status-pending {
  color: #F59E0B !important;
  background: #FFF9EB !important;
  border-color: #FDE68A !important;
}
</style>
