<template>
  <div class="create-survey">
    <h2>创建问卷</h2>
    <el-tabs v-model="mode">
      <el-tab-pane label="手动创建" name="manual">
        <SurveyEditor ref="editorRef" :initial="surveyData" />
      </el-tab-pane>
      <el-tab-pane label="AI 生成" name="ai">
        <el-form :model="aiForm" label-width="80px">
          <el-form-item label="需求描述">
            <el-input v-model="aiForm.prompt" type="textarea" :rows="4" placeholder="描述你的调研需求，例如：我想了解30-40岁职场人对远程办公的态度，大约10个问题" />
          </el-form-item>
          <el-form-item label="行业">
            <el-select v-model="aiForm.industry" placeholder="选择行业（可选）">
              <el-option label="互联网" value="互联网" />
              <el-option label="教育" value="教育" />
              <el-option label="金融" value="金融" />
              <el-option label="医疗" value="医疗" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目数量">
            <el-input-number v-model="aiForm.questionCount" :min="5" :max="20" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="aiLoading" @click="handleAiGenerate">生成问卷</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="模板创建" name="template">
        <el-radio-group v-model="templateCategory" @change="fetchTemplates" style="margin-bottom:16px">
          <el-radio-button label="market">市场调研</el-radio-button>
          <el-radio-button label="academic">学术研究</el-radio-button>
          <el-radio-button label="hr">人力资源</el-radio-button>
        </el-radio-group>
        <el-row :gutter="16">
          <el-col :span="8" v-for="tpl in templates" :key="tpl.id">
            <el-card shadow="hover" @click="handleUseTemplate(tpl)">
              <h4>{{ tpl.title }}</h4>
              <p>{{ tpl.description }}</p>
              <el-tag size="small">{{ tpl.questionCount }}题 · 约{{ tpl.estimatedMinutes }}分钟</el-tag>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    <div class="actions" v-if="mode==='manual'">
      <el-button @click="$router.back()">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存草稿</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { surveyApi } from '@/api/survey'
import { ElMessage } from 'element-plus'
import SurveyEditor from '@/components/SurveyEditor.vue'

const router = useRouter()
const mode = ref('manual')
const saving = ref(false)
const aiLoading = ref(false)
const templates = ref([])
const templateCategory = ref('market')
const editorRef = ref()

const surveyData = reactive({ title: '', description: '', questions: [] })
const aiForm = reactive({ prompt: '', industry: '', questionCount: 10 })

async function handleSave() {
  const data = editorRef.value?.getData()
  if (!data?.title) { ElMessage.warning('请输入问卷标题'); return }
  saving.value = true
  try {
    const res = await surveyApi.create(data)
    ElMessage.success('保存成功')
    router.push('/surveys/' + res.id)
  } catch {} finally { saving.value = false }
}

async function handleAiGenerate() {
  if (!aiForm.prompt) { ElMessage.warning('请输入需求描述'); return }
  aiLoading.value = true
  try {
    const res = await surveyApi.aiGenerate(aiForm)
    mode.value = 'manual'
    await new Promise(r => setTimeout(r, 100))
    editorRef.value?.loadData(res.surveyDraft)
    ElMessage.success('AI 生成完成，请检查并修改')
  } catch {} finally { aiLoading.value = false }
}

async function fetchTemplates() {
  try { templates.value = await surveyApi.getTemplates(templateCategory.value) } catch {}
}

async function handleUseTemplate(tpl) {
  try {
    const detail = await surveyApi.getTemplate(tpl.id)
    mode.value = 'manual'
    await new Promise(r => setTimeout(r, 100))
    editorRef.value?.loadData(detail)
  } catch {}
}

fetchTemplates()
</script>

<style scoped>
.actions { margin-top: 24px; display: flex; gap: 12px; justify-content: flex-end; }
</style>
