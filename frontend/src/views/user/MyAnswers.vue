<template>
  <div class="my-answers">
    <h1 class="page-title">我的回答</h1>
    <el-table :data="list" v-loading="loading" style="width:100%;margin-top:16px" stripe>
      <el-table-column prop="survey_title" label="问卷标题" min-width="200">
        <template #default="{ row }">
          <router-link :to="'/marketplace/' + row.survey_id" class="survey-link">{{ row.survey_title }}</router-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'approved'" type="success" size="small">审核通过</el-tag>
          <el-tag v-else-if="row.status === 'submitted'" type="warning" size="small">审核中</el-tag>
          <el-tag v-else-if="row.status === 'in_progress'" type="info" size="small">未完成</el-tag>
          <el-tag v-else-if="row.status === 'rejected'" type="danger" size="small">未通过</el-tag>
          <el-tag v-else-if="row.status === 'expired'" type="info" size="small">已过期</el-tag>
          <el-tag v-else size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reward_amount" label="奖励" width="100">
        <template #default="{ row }">
          <span v-if="row.reward_amount > 0" style="color:#F59E0B;font-weight:600">¥{{ row.reward_amount }}</span>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="quality_score" label="质量评分" width="100">
        <template #default="{ row }">
          <span v-if="row.quality_score">{{ row.quality_score }}</span>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="duration_seconds" label="耗时" width="90">
        <template #default="{ row }">
          {{ formatDuration(row.duration_seconds) }}
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="提交时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.created_at) }}
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="暂无回答记录" />
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
</script>

<style scoped>
.page-title { margin: 0 0 8px; font-size: 24px; color: #134E4A; font-weight: 700; }
.survey-link { color: #0D9488; text-decoration: none; font-weight: 500; }
.survey-link:hover { text-decoration: underline; }
</style>
