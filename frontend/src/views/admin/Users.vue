<template>
  <div class="admin-users">
    <h1 class="page-title">用户管理</h1>
    <el-table :data="users" stripe v-loading="loading" class="styled-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="昵称" width="150" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="level" label="等级" width="80" />
      <el-table-column prop="reputation" label="信誉分" width="80" />
      <el-table-column prop="balance" label="余额" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag v-if="row.status==='normal'" type="success">正常</el-tag>
          <el-tag v-else-if="row.status==='frozen'" type="warning">冻结</el-tag>
          <el-tag v-else-if="row.status==='banned'" type="danger">封禁</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="140">
        <template #default="{row}">
          <el-button v-if="row.status==='normal'||row.status==='frozen'" size="small" type="danger" @click="handleBan(row)">封禁</el-button>
          <el-button v-else size="small" type="success" @click="handleUnban(row)">解封</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev,pager,next" @current-change="fetchUsers" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref([])
const page = ref(1)
const total = ref(0)

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try { const res = await adminApi.getUsers({ page: page.value, size: 20 }); users.value = res.list; total.value = res.total } catch {} finally { loading.value = false }
}

async function handleBan(row) {
  await ElMessageBox.prompt('封禁原因', '封禁用户', { type: 'warning' })
  await adminApi.banUser(row.id, '违规')
  ElMessage.success('已封禁')
  fetchUsers()
}

async function handleUnban(row) {
  await adminApi.unbanUser(row.id)
  ElMessage.success('已解封')
  fetchUsers()
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
