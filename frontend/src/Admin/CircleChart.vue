<script setup lang="ts">

</script>

<template>
  <div id="main"></div>
  <script type="text/javascript">
    var myChart = echarts.init(document.getElementById('main'), null, {
      width: 600,
      height: 400
    });
  </script>
</template>

<style scoped>
#main,
html,
body {
  width: 100%;
}
#main {
  height: 400px;
}
</style>