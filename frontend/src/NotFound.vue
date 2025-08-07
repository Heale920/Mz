<template>
  <div class="no-access">
    <h2>⛔ 无权限访问</h2>
    <p>你没有权限查看该页面，请联系管理员或返回首页。</p>
    <router-link to="/" class="btn">返回首页</router-link>
  </div>
</template>

<script setup>
</script>

<style scoped>
.no-access {
  text-align: center;
  margin-top: 150px;
  color: #e74c3c;
}
.btn {
  margin-top: 20px;
  display: inline-block;
  padding: 10px 20px;
  background: #64ffda;
  color: #0a192f;
  border-radius: 6px;
  text-decoration: none;
}
</style>
