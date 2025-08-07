<template>
  <div class="statistics-page">
    <!-- 顶部概览数据 -->
    <el-row :gutter="20" class="stat-top">
      <el-col :span="3" v-for="item in overview" :key="item.label">
        <el-card>
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户增长图 -->
    <el-card class="chart-card">
      <div ref="userChart" style="width: 100%; height: 400px;"></div>
    </el-card>

    <!-- 标签文章数量图（饼图） -->
    <el-card class="chart-card">
      <div ref="tagChart" style="width: 100%; height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const overview = ref([])
const userChart = ref(null)
const tagChart = ref(null)

onMounted(async () => {
  try {
    // === 总览数据 ===
    const overviewRes = await axios.get('http://localhost:8081/statistics/overview')
    const overviewData = overviewRes.data.data || {}
    overview.value = [
      { label: '用户数', value: overviewData.userCount || 0 },
      { label: '管理员数', value: overviewData.adminCount || 0 },
      { label: '文章数', value: overviewData.articleCount || 0 },
      { label: '标签数', value: overviewData.tagCount || 0 },
      { label: '评论数', value: overviewData.commentCount || 0 },
      { label: '总浏览量', value: overviewData.viewCount || 0 },
      { label: '点赞数', value: overviewData.likeCount || 0 }
    ]

    // === 用户增长折线图 ===
    const userGrowthRes = await axios.get('http://localhost:8081/statistics/usergrowth')
    const rawUserData = userGrowthRes.data.data
    const userData = rawUserData.filter(i => i && typeof i === 'object')
    const userChartInstance = echarts.init(userChart.value)
    userChartInstance.setOption({
      title: { text: '用户增长趋势' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: userData.map(i => i.date) },
      yAxis: { type: 'value' },
      series: [
        {
          name: '注册用户数',
          data: userData.map(i => i.count),
          type: 'line',
          smooth: true
        }
      ]
    })

    // === 标签文章数量饼图 ===
    const tagRes = await axios.get('http://localhost:8081/statistics/TagArticleCount')
    const tagData = (tagRes.data.data || []).filter(i => i && typeof i === 'object')

    const tagChartInstance = echarts.init(tagChart.value)
    tagChartInstance.setOption({
      title: {
        text: '标签文章占比',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '文章数',
          type: 'pie',
          radius: '60%',
          data: tagData.map(i => ({
            name: i.tagName,
            value: i.articleCount
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  } catch (error) {
    console.error('数据统计加载失败：', error)
  }
})
</script>

<style scoped>
.stat-top {
  margin-bottom: 20px;
}
.label {
  font-size: 14px;
  color: #999;
}
.value {
  font-size: 24px;
  font-weight: bold;
}
.chart-card {
  margin-top: 30px;
}
</style>
