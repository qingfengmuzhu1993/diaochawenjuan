<template>
  <div class="admin-finance">
    <h1 class="page-title">财务管理</h1>
    <el-table :data="transactions" stripe v-loading="loading" class="styled-table">
      <el-table-column prop="transactionNo" label="流水号" width="180" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{row}">
          <el-tag v-if="row.type==='recharge'" type="success">充值</el-tag>
          <el-tag v-else-if="row.type==='reward'" type="warning">奖励</el-tag>
          <el-tag v-else-if="row.type==='withdraw'" type="info">提现</el-tag>
          <span v-else>{{ row.type }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column prop="balanceBefore" label="交易前余额" width="120" />
      <el-table-column prop="balanceAfter" label="交易后余额" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="时间" width="170" />
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev,pager,next" @current-change="fetchTransactions" style="margin-top:20px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const transactions = ref([])
const page = ref(1)
const total = ref(0)

onMounted(() => fetchTransactions())

async function fetchTransactions() {
  loading.value = true
  try { const res = await adminApi.getTransactions({ page: page.value, size: 20 }); transactions.value = res.list; total.value = res.total } catch {} finally { loading.value = false }
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
