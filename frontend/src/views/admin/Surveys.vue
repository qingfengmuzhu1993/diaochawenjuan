<template>
  <div class="admin-surveys">
    <h1 class="page-title">问卷管理</h1>
    <el-radio-group v-model="filter" @change="fetchSurveys" style="margin-bottom:16px">
      <el-radio-button value="pending">待审核</el-radio-button>
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="approved">已通过</el-radio-button>
      <el-radio-button value="rejected">已拒绝</el-radio-button>
    </el-radio-group>
    <el-table :data="surveys" stripe v-loading="loading" class="styled-table">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="userId" label="创建者ID" width="100" />
      <el-table-column prop="totalQuestions" label="题目数" width="80" />
      <el-table-column prop="rewardPerResponse" label="奖励/份" width="100" />
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{row}">
          <el-tag v-if="row.auditStatus==='approved'" type="success">已通过</el-tag>
          <el-tag v-else-if="row.auditStatus==='rejected'" type="danger">已拒绝</el-tag>
          <el-tag v-else type="warning">待审核</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button v-if="row.auditStatus==='pending'" size="small" type="success" @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.auditStatus==='pending'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          <el-button size="small" type="info" @click="handleRemove(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev,pager,next" @current-change="fetchSurveys" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const surveys = ref([])
const page = ref(1)
const total = ref(0)
const filter = ref('pending')

onMounted(() => fetchSurveys())

async function fetchSurveys() {
  loading.value = true
  try { const res = await adminApi.getSurveys({ page: page.value, size: 20, auditStatus: filter.value }); surveys.value = res.list; total.value = res.total } catch {} finally { loading.value = false }
}

async function handleApprove(row) { await adminApi.approveSurvey(row.id); ElMessage.success('已通过'); fetchSurveys() }
async function handleReject(row) {
  await ElMessageBox.prompt('拒绝原因', '拒绝问卷')
  await adminApi.rejectSurvey(row.id, '不符合要求')
  ElMessage.success('已拒绝')
  fetchSurveys()
}
async function handleRemove(row) { await adminApi.removeSurvey(row.id); ElMessage.success('已下架'); fetchSurveys() }
</script>

<style scoped>
.styled-table {
  border-radius: 16px;
  overflow: hidden;
}
.styled-table :deep(.el-table__header th) {
  background: #E0FAF2;
  color: #134E4A;
  font-weight: 600;
}
</style>
