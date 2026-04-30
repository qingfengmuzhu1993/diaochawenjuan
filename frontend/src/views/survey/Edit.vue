<template>
  <div class="edit-survey">
    <h2>编辑问卷</h2>
    <SurveyEditor ref="editorRef" v-if="loaded" :initial="surveyData" />
    <div class="actions" v-if="loaded">
      <el-button @click="$router.back()">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { surveyApi } from '@/api/survey'
import { ElMessage } from 'element-plus'
import SurveyEditor from '@/components/SurveyEditor.vue'

const route = useRoute()
const router = useRouter()
const loaded = ref(false)
const saving = ref(false)
const surveyData = reactive({ title: '', description: '', questions: [] })
const editorRef = ref()

onMounted(async () => {
  try {
    const detail = await surveyApi.getDetail(route.params.id)
    Object.assign(surveyData, {
      title: detail.title,
      description: detail.description,
      coverImage: detail.coverImage,
      closingMessage: detail.closingMessage,
      timeLimitMinutes: detail.timeLimitMinutes,
      maxAttempts: detail.maxAttempts,
      isAnonymous: detail.isAnonymous,
      rewardPerResponse: detail.rewardPerResponse,
      targetQuota: detail.targetQuota,
      questions: detail.questions || [],
    })
    loaded.value = true
  } catch {}
})

async function handleSave() {
  const data = editorRef.value?.getData()
  if (!data?.title) { ElMessage.warning('请输入问卷标题'); return }
  saving.value = true
  try {
    await surveyApi.update(route.params.id, data)
    ElMessage.success('保存成功')
    router.push('/surveys/' + route.params.id)
  } catch {} finally { saving.value = false }
}
</script>

<style scoped>
.actions { margin-top: 24px; display: flex; gap: 12px; justify-content: flex-end; }
</style>
