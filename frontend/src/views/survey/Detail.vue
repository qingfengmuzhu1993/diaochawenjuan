<template>
  <div class="survey-detail" v-loading="loading">
    <div class="header">
      <h2>{{ survey.title }}</h2>
      <div class="header-actions">
        <span v-if="survey.status==='draft'"><el-button @click="$router.push('/surveys/'+survey.id+'/edit')">编辑</el-button></span>
        <span v-if="survey.status==='published'"><el-button type="warning" @click="handleClose">关闭问卷</el-button></span>
        <el-button type="info" @click="$router.back()">返回</el-button>
      </div>
    </div>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="状态">
        <el-tag v-if="survey.status==='draft'">草稿</el-tag>
        <el-tag v-else-if="survey.status==='published'" type="success">发布中</el-tag>
        <el-tag v-else-if="survey.status==='closed'" type="info">已结束</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="题目数">{{ survey.totalQuestions }}</el-descriptions-item>
      <el-descriptions-item label="已回收">{{ survey.totalResponses }}</el-descriptions-item>
      <el-descriptions-item label="目标配额">{{ survey.targetQuota || '不限' }}</el-descriptions-item>
      <el-descriptions-item label="每份奖励">¥{{ survey.rewardPerResponse || 0 }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ survey.createdAt }}</el-descriptions-item>
    </el-descriptions>

    <h3 style="margin-top:24px">题目列表</h3>
    <div v-for="(q, i) in survey.questions" :key="q.id" class="question-item">
      <el-card>
        <template #header>
          <span>{{ i + 1 }}. {{ q.content }}</span>
          <el-tag size="small" style="margin-left:8px">{{ typeLabel(q.type) }}</el-tag>
          <el-tag v-if="q.required===1" size="small" type="danger" style="margin-left:4px">必答</el-tag>
        </template>
        <div v-if="q.options">
          <el-radio-group v-if="q.type==='single'" disabled>
            <el-radio v-for="opt in parseOptions(q.options)" :key="opt.id">{{ opt.text }}</el-radio>
          </el-radio-group>
          <el-checkbox-group v-if="q.type==='multiple'" disabled>
            <el-checkbox v-for="opt in parseOptions(q.options)" :key="opt.id">{{ opt.text }}</el-checkbox>
          </el-checkbox-group>
        </div>
        <div v-else-if="q.type==='rating'">
          <el-rate disabled :max="5" />
        </div>
        <div v-else-if="q.type==='essay'">
          <el-input type="textarea" disabled placeholder="简答题" />
        </div>
        <div v-else>
          <el-input disabled :placeholder="'请输入'" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { surveyApi } from '@/api/survey'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const survey = ref({})

onMounted(async () => {
  loading.value = true
  try { survey.value = await surveyApi.getDetail(route.params.id) } catch {} finally { loading.value = false }
})

function typeLabel(type) {
  const map = { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '简答', rating: '评分', matrix: '矩阵', ranking: '排序' }
  return map[type] || type
}

function parseOptions(opts) {
  try { return typeof opts === 'string' ? JSON.parse(opts) : opts } catch { return [] }
}

async function handleClose() {
  await ElMessageBox.confirm('确定关闭该问卷？', '提示', { type: 'warning' })
  await surveyApi.close(route.params.id)
  ElMessage.success('已关闭')
  survey.value.status = 'closed'
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.question-item { margin-bottom: 12px; }
</style>
