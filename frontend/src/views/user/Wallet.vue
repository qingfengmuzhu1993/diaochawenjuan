<template>
  <div class="wallet">
    <h1 class="page-title">我的钱包</h1>
    <div class="stat-row">
      <div class="stat-card">
        <span class="stat-label">可用余额</span>
        <span class="stat-value">¥{{ wallet.balance?.toFixed(2) || '0.00' }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">冻结金额</span>
        <span class="stat-value frozen">¥{{ wallet.frozenBalance?.toFixed(2) || '0.00' }}</span>
      </div>
    </div>
    <div class="actions" style="margin:20px 0">
      <el-button type="primary" round @click="showRecharge=true">充值</el-button>
      <el-button round @click="showWithdraw=true">提现</el-button>
    </div>
    <h3>交易记录</h3>
    <el-table :data="bills" stripe>
      <el-table-column prop="transactionNo" label="流水号" width="180" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{row}">
          <el-tag v-if="row.type==='recharge'" type="success">充值</el-tag>
          <el-tag v-else-if="row.type==='reward'" type="warning">奖励</el-tag>
          <el-tag v-else-if="row.type==='withdraw'" type="info">提现</el-tag>
          <span v-else>{{ row.type }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column prop="balanceAfter" label="余额" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="时间" width="170" />
    </el-table>

    <el-dialog v-model="showRecharge" title="充值" width="300px">
      <el-input-number v-model="rechargeAmount" :min="10" :max="5000" style="width:100%" />
      <template #footer><el-button @click="showRecharge=false">取消</el-button><el-button type="primary" @click="handleRecharge">确认充值</el-button></template>
    </el-dialog>
    <el-dialog v-model="showWithdraw" title="提现" width="300px">
      <el-input-number v-model="withdrawAmount" :min="10" :max="5000" style="width:100%" />
      <template #footer><el-button @click="showWithdraw=false">取消</el-button><el-button type="primary" @click="handleWithdraw">确认提现</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const wallet = ref({ balance: 0, frozenBalance: 0 })
const bills = ref([])
const showRecharge = ref(false)
const showWithdraw = ref(false)
const rechargeAmount = ref(100)
const withdrawAmount = ref(10)

onMounted(async () => {
  try { wallet.value = await userApi.getWallet() } catch {}
  try { bills.value = (await userApi.getBills(1, 20)).list } catch {}
})

async function handleRecharge() {
  try { await userApi.recharge(rechargeAmount.value); showRecharge.value = false; ElMessage.success('充值成功'); onMounted() } catch {}
}
async function handleWithdraw() {
  try { await userApi.withdraw(withdrawAmount.value); showWithdraw.value = false; ElMessage.success('提现申请已提交'); onMounted() } catch {}
}
</script>

<style scoped>
.stat-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #E8F5EF;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #87A697;
  display: block;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2DD4A8;
}

.stat-value.frozen {
  color: #87A697;
}
</style>
