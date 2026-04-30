<template>
  <div class="survey-editor">
    <el-form label-width="100px">
      <el-form-item label="问卷标题" required>
        <el-input v-model="form.title" placeholder="请输入问卷标题" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="问卷描述">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="选填，介绍调研目的和填写须知" />
      </el-form-item>
      <el-form-item label="结束语">
        <el-input v-model="form.closingMessage" placeholder="提交后显示的文字" />
      </el-form-item>
    </el-form>

    <h3 style="margin:20px 0 12px">题目管理</h3>
    <div v-for="(q, i) in form.questions" :key="i" class="question-editor">
      <el-card>
        <template #header>
          <span>第 {{ i + 1 }} 题</span>
          <el-select v-model="q.type" style="width:120px;margin:0 12px">
            <el-option v-for="t in questionTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <el-switch v-model="q.required" active-text="必答" />
          <el-button type="danger" size="small" @click="removeQuestion(i)" style="float:right" circle>
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
        <el-input v-model="q.content" placeholder="题目内容" style="margin-bottom:8px" />

        <div v-if="needsOptions(q.type)" class="options-section">
          <div v-for="(opt, oi) in parseOptions(q)" :key="oi" class="option-row">
            <el-input v-model="opt.text" :placeholder="'选项' + (oi+1)" style="width:300px" />
            <el-button type="danger" size="small" @click="removeOption(q, oi)" circle><el-icon><Delete /></el-icon></el-button>
          </div>
          <el-button size="small" @click="addOption(q)" :disabled="parseOptions(q).length >= 20">+ 添加选项</el-button>
        </div>

        <div v-if="q.type==='rating'" style="margin-top:8px">
          <span>分值范围：</span>
          <el-input-number v-model="q.maxRating" :min="3" :max="10" size="small" />
        </div>

        <div v-if="q.type==='matrix'" style="margin-top:8px">
          <span>矩阵配置（占位，在详情中配置行列）</span>
        </div>
      </el-card>
    </div>

    <el-button type="primary" style="margin-top:12px;width:100%" @click="addQuestion">
      <el-icon><Plus /></el-icon>添加题目
    </el-button>

    <el-divider />
    <el-form label-width="100px">
      <el-form-item label="答题时间限制">
        <el-input-number v-model="form.timeLimitMinutes" :min="0" :max="120" /> 分钟（0=不限）
      </el-form-item>
      <el-form-item label="每人答题次数">
        <el-input-number v-model="form.maxAttempts" :min="1" :max="10" />
      </el-form-item>
      <el-form-item label="匿名回答">
        <el-switch v-model="form.isAnonymous" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="目标配额">
        <el-input-number v-model="form.targetQuota" :min="0" /> 份
      </el-form-item>
      <el-form-item label="每份奖励(元)">
        <el-input-number v-model="form.rewardPerResponse" :min="0" :precision="2" :step="0.5" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'

const props = defineProps({ initial: { type: Object, default: () => ({}) } })

const form = reactive({
  title: '', description: '', closingMessage: '感谢您的参与！',
  timeLimitMinutes: 0, maxAttempts: 1, isAnonymous: 0, targetQuota: 100,
  rewardPerResponse: 1.0, questions: [],
})

const questionTypes = [
  { label: '单选题', value: 'single' }, { label: '多选题', value: 'multiple' },
  { label: '判断题', value: 'judge' }, { label: '填空题', value: 'fill' },
  { label: '简答题', value: 'essay' }, { label: '评分题', value: 'rating' },
  { label: '矩阵题', value: 'matrix' }, { label: '排序题', value: 'ranking' },
]

function needsOptions(type) { return ['single','multiple','judge','ranking'].includes(type) }

function parseOptions(q) {
  if (!q._options) {
    if (typeof q.options === 'string') {
      try { q._options = JSON.parse(q.options) } catch { q._options = [] }
    } else if (Array.isArray(q.options)) {
      q._options = q.options.map((o, i) => typeof o === 'string' ? { id: i+1, text: o } : { ...o })
    } else {
      q._options = []
    }
    if (q._options.length === 0) {
      q._options = [{ id: 1, text: '' }, { id: 2, text: '' }]
    }
  }
  return q._options
}

function addOption(q) {
  const opts = parseOptions(q)
  opts.push({ id: opts.length + 1, text: '' })
}

function removeOption(q, idx) { parseOptions(q).splice(idx, 1) }

function addQuestion() {
  form.questions.push({ type: 'single', content: '', required: 1, orderIndex: form.questions.length, _options: [{ id: 1, text: '' }, { id: 2, text: '' }] })
}

function removeQuestion(idx) { form.questions.splice(idx, 1) }

function getData() {
  const data = { ...form }
  data.questions = form.questions.map((q, i) => ({
    type: q.type, content: q.content, required: q.required === true || q.required === 1 ? 1 : 0,
    orderIndex: i, options: needsOptions(q.type) ? JSON.stringify(parseOptions(q)) : null,
  }))
  return data
}

function loadData(data) {
  Object.assign(form, {
    title: data.title || '', description: data.description || '',
    closingMessage: data.closingMessage || '感谢您的参与！',
    timeLimitMinutes: data.timeLimitMinutes || 0, maxAttempts: data.maxAttempts || 1,
    isAnonymous: data.isAnonymous || 0, targetQuota: data.targetQuota || 100,
    rewardPerResponse: data.rewardPerResponse || 1.0,
    questions: (data.questions || []).map(q => ({
      ...q, _options: q.options ? (typeof q.options === 'string' ? JSON.parse(q.options) : q.options) : [{ id: 1, text: '' }, { id: 2, text: '' }]
    })),
  })
}

onMounted(() => {
  if (props.initial?.questions?.length) loadData(props.initial)
  else if (props.initial?.title) loadData(props.initial)
})

defineExpose({ getData, loadData })
</script>

<style scoped>
.question-editor { margin-bottom: 12px; }
.option-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
</style>
