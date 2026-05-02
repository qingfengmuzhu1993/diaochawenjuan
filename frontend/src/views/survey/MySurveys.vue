<template>
  <div class="my-surveys">
    <div class="hero-section">
      <div class="hero-left">
        <h1 class="page-title">我的问卷</h1>
        <p class="hero-subtitle">管理和追踪你创建的所有问卷</p>
      </div>
      <el-button type="primary" size="large" round @click="$router.push('/surveys/create')" class="create-btn">
        <el-icon><Plus /></el-icon>创建问卷
      </el-button>
    </div>

    <div class="toolbar">
      <div class="filter-pills">
        <button v-for="tab in tabs" :key="tab.value"
          class="filter-pill" :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value; fetchSurveys()">{{ tab.label }}</button>
      </div>
      <transition name="slide-down">
        <el-button v-if="selectedIds.length > 0" type="danger" round @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>批量删除 ({{ selectedIds.length }})
        </el-button>
      </transition>
    </div>

    <el-table :data="surveys" v-loading="loading" stripe class="styled-table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" />
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="title-cell">
            <el-link type="primary" @click="$router.push('/surveys/' + row.id)">{{ row.title }}</el-link>
            <el-tag v-if="row.totalResponses > 0" size="small" type="success" effect="plain" round class="resp-tag">
              {{ row.totalResponses }} 份回收
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.status==='draft'" effect="plain" round class="status-draft">草稿</el-tag>
          <el-tag v-else-if="row.status==='published'" effect="dark" round class="status-pub">发布中</el-tag>
          <el-tag v-else-if="row.status==='closed'" effect="plain" round type="info">已结束</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalQuestions" label="题目" width="80" align="center" />
      <el-table-column prop="rewardPerResponse" label="奖励/份" width="110" align="right">
        <template #default="{ row }">
          <span v-if="row.rewardPerResponse > 0" class="reward-cell">¥{{ row.rewardPerResponse }}</span>
          <span v-else style="color:var(--app-text-muted)">未设置</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="175">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <template v-if="row.status==='draft'">
              <el-button size="small" round @click="$router.push('/surveys/'+row.id+'/edit')">编辑</el-button>
              <el-button size="small" type="success" round @click="handlePublish(row)">发布</el-button>
            </template>
            <template v-if="row.status==='published'">
              <el-button size="small" type="primary" round @click="$router.push('/analytics/'+row.id)">分析</el-button>
              <el-button size="small" type="warning" round @click="handleClose(row)">关闭</el-button>
            </template>
            <el-button link type="primary" size="small" @click="handleDuplicate(row)">复制</el-button>
            <el-button v-if="row.status!=='archived'" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev, pager, next" @current-change="fetchSurveys" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { surveyApi } from '@/api/survey'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

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

function formatTime(dt) {
  if (!dt) return ''
  return dt.substring(0, 16).replace('T', ' ')
}
</script>

<style scoped>
/* ====== Hero Section ====== */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}
.hero-left { flex: 1; }
.hero-subtitle {
  margin: 8px 0 0;
  font-size: 15px;
  color: var(--app-text-muted);
  font-weight: 400;
}
.create-btn {
  font-weight: 600 !important;
  padding: 12px 24px !important;
  font-size: 15px;
}

/* ====== Toolbar ====== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  min-height: 36px;
}

/* ====== Table Customizations ====== */
.title-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.resp-tag {
  font-weight: 500;
  flex-shrink: 0;
}
.reward-cell {
  color: #F59E0B;
  font-weight: 700;
  font-size: 14px;
}
.time-cell {
  color: var(--app-text-muted);
  font-size: 13px;
}
.action-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

/* status tag colors */
.status-draft {
  color: #5F8B7A !important;
  background: #F0FAF5 !important;
  border-color: #CCE4D6 !important;
}
.status-pub {
  background: #0D9488 !important;
  border-color: #0D9488 !important;
}

/* ====== Pagination ====== */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  padding: 8px 0;
}

/* ====== Transitions ====== */
.slide-down-enter-active { transition: all 0.25s ease-out; }
.slide-down-leave-active { transition: all 0.15s ease-in; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
