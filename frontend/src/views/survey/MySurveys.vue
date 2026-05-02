<template>
  <div class="my-surveys">
    <div class="page-header">
      <h1 class="page-title">我的问卷</h1>
      <el-button type="primary" round @click="$router.push('/surveys/create')">
        <el-icon><Plus /></el-icon>创建问卷
      </el-button>
    </div>
    <div class="filter-pills" style="margin-bottom:20px">
      <button v-for="tab in tabs" :key="tab.value"
        class="filter-pill" :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value; fetchSurveys()">{{ tab.label }}</button>
    </div>
    <div v-if="selectedIds.length > 0" style="margin-bottom:12px">
      <el-button type="danger" size="small" @click="handleBatchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>
    <el-table :data="surveys" v-loading="loading" stripe class="styled-table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push('/surveys/' + row.id)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status==='draft'" round>草稿</el-tag>
          <el-tag v-else-if="row.status==='published'" type="success" round>发布中</el-tag>
          <el-tag v-else-if="row.status==='closed'" type="info" round>已结束</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalResponses" label="回收" width="80" />
      <el-table-column prop="totalQuestions" label="题目" width="80" />
      <el-table-column prop="rewardPerResponse" label="奖励/份" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status==='draft'" size="small" round @click="$router.push('/surveys/'+row.id+'/edit')">编辑</el-button>
          <el-button v-if="row.status==='draft'" size="small" type="success" round @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status==='published'" size="small" type="primary" round @click="$router.push('/analytics/'+row.id)">分析</el-button>
          <el-button v-if="row.status==='published'" size="small" type="warning" round @click="handleClose(row)">关闭</el-button>
          <el-button link type="primary" size="small" @click="handleDuplicate(row)">复制</el-button>
          <el-button v-if="row.status!=='archived'" size="small" type="danger" round @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev, pager, next" @current-change="fetchSurveys" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { surveyApi } from '@/api/survey'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const surveys = ref([])
const activeTab = ref('')
const page = ref(1)
const total = ref(0)
const tabs = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'draft' },
  { label: '发布中', value: 'published' },
  { label: '已结束', value: 'closed' },
]

onMounted(() => fetchSurveys())

async function fetchSurveys() {
  loading.value = true
  try {
    const res = await surveyApi.getMySurveys({ status: activeTab.value, page: page.value, size: 20 })
    surveys.value = res.list
    total.value = res.total
  } catch {} finally { loading.value = false }
}

async function handlePublish(row) {
  await ElMessageBox.prompt('请输入每份奖励金额', '发布问卷', { inputType: 'number', inputValue: '0' })
  await surveyApi.publish(row.id, { dispatchType: 'public', rewardTotalBudget: 0 })
  ElMessage.success('发布成功')
  fetchSurveys()
}

async function handleClose(row) {
  await ElMessageBox.confirm('确定关闭该问卷？', '提示', { type: 'warning' })
  await surveyApi.close(row.id)
  ElMessage.success('已关闭')
  fetchSurveys()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该问卷？', '提示', { type: 'warning' })
  await surveyApi.delete(row.id)
  ElMessage.success('已删除')
  fetchSurveys()
}

const selectedIds = ref([])

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm('确定删除选中的' + selectedIds.value.length + '份问卷吗？', '批量删除')
    for (const id of selectedIds.value) {
      await surveyApi.delete(id)
    }
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchSurveys()
  } catch {}
}

async function handleDuplicate(row) {
  try {
    await surveyApi.duplicateSurvey(row.id)
    ElMessage.success('复制成功')
    fetchSurveys()
  } catch {}
}
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
