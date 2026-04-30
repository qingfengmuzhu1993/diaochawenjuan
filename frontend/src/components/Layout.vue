<template>
  <div class="app-layout">
    <Sidebar />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #F0FAF5;
}

.main-content {
  margin-left: 220px;
  flex: 1;
  padding: 24px;
  min-width: 0;
}
</style>
