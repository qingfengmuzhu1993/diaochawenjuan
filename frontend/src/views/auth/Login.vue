<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="brand-area">
        <div class="brand-icon">S</div>
        <h1>SmartSurvey</h1>
        <p>让每一份问卷都有价值</p>
      </div>
    </div>
    <div class="auth-right">
      <div class="form-wrapper">
        <h2>欢迎回来</h2>
        <p class="form-subtitle">登录你的账号继续使用</p>
        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
          <el-form-item prop="account">
            <el-input v-model="form.account" placeholder="手机号/邮箱" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">登录</el-button>
          </el-form-item>
        </el-form>
        <p class="auth-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.login(form.account, form.password)
    ElMessage.success('登录成功')
    router.push('/marketplace')
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, #2DD4A8, #14B8A6, #0D9488);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-area {
  text-align: center;
  color: #FFFFFF;
}

.brand-icon {
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  margin: 0 auto 20px;
}

.brand-area h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px;
}

.brand-area p {
  font-size: 16px;
  opacity: 0.85;
}

.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
}

.form-wrapper {
  width: 380px;
}

.form-wrapper h2 {
  font-size: 28px;
  font-weight: 700;
  color: #134E4A;
  margin: 0 0 4px;
}

.form-subtitle {
  color: #87A697;
  margin: 0 0 32px;
  font-size: 14px;
}

.auth-link {
  text-align: center;
  color: #87A697;
  font-size: 14px;
}

.auth-link a {
  color: #2DD4A8;
  font-weight: 600;
  text-decoration: none;
}
</style>
