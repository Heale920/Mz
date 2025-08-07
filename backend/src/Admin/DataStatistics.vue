<template>
  <div class="statistics-container">
    <!-- 总览卡片区 -->
    <div class="overview-cards">
      <div class="card">
        <div class="card-icon user"></div>
        <div class="card-content">
          <div class="card-title">用户数</div>
          <div class="card-value">1234</div>
        </div>
      </div>
      <div class="card">
        <div class="card-icon article"></div>
        <div class="card-content">
          <div class="card-title">文章数</div>
          <div class="card-value">567</div>
        </div>
      </div>
      <div class="card">
        <div class="card-icon tag"></div>
        <div class="card-content">
          <div class="card-title">标签数</div>
          <div class="card-value">89</div>
        </div>
      </div>
      <div class="card">
        <div class="card-icon admin"></div>
        <div class="card-content">
          <div class="card-title">管理员数</div>
          <div class="card-value">3</div>
        </div>
      </div>
    </div>
    <!-- 图表区 -->
    <div class="charts-grid">
      <div class="chart-box"><div ref="userTrendChart" class="chart"></div></div>
      <div class="chart-box"><div ref="categoryPieChart" class="chart"></div></div>
      <div class="chart-box"><div ref="tagBarChart" class="chart"></div></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { ref, onMounted, onUnmounted } from 'vue';

export default {
  setup() {
    // 图表 DOM 引用
    const userTrendChart = ref(null);
    const categoryPieChart = ref(null);
    const tagBarChart = ref(null);
    // 图表实例
    let userTrendInstance = null;
    let categoryPieInstance = null;
    let tagBarInstance = null;

    // mock 数据
    const userTrendData = {
      dates: ['6-01', '6-02', '6-03', '6-04', '6-05', '6-06', '6-07'],
      counts: [10, 20, 35, 50, 65, 80, 100]
    };
    const categoryPieData = [
      { value: 335, name: '技术' },
      { value: 310, name: '生活' },
      { value: 234, name: '随笔' },
      { value: 135, name: '教程' },
      { value: 154, name: '其他' }
    ];
    const tagBarData = {
      tags: ['Vue', 'Java', 'Spring', '前端', '后端', '随笔', '生活'],
      counts: [120, 110, 90, 80, 70, 60, 50]
    };

    onMounted(() => {
      // 用户增长趋势
      userTrendInstance = echarts.init(userTrendChart.value);
      userTrendInstance.setOption({
        title: { text: '用户增长趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: userTrendData.dates },
        yAxis: { type: 'value' },
        series: [{
          name: '用户数',
          type: 'line',
          data: userTrendData.counts,
          smooth: true,
          areaStyle: {}
        }]
      });
      // 文章分类分布
      categoryPieInstance = echarts.init(categoryPieChart.value);
      categoryPieInstance.setOption({
        title: { text: '文章分类分布', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          name: '分类',
          type: 'pie',
          radius: '60%',
          data: categoryPieData,
          emphasis: {
            itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
          }
        }]
      });
      // 热门标签排行
      tagBarInstance = echarts.init(tagBarChart.value);
      tagBarInstance.setOption({
        title: { text: '热门标签排行', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: tagBarData.tags },
        yAxis: { type: 'value' },
        series: [{
          name: '文章数',
          type: 'bar',
          data: tagBarData.counts,
          itemStyle: { color: '#5470C6' }
        }]
      });
      // 响应式
      window.addEventListener('resize', resizeCharts);
    });

    function resizeCharts() {
      userTrendInstance && userTrendInstance.resize();
      categoryPieInstance && categoryPieInstance.resize();
      tagBarInstance && tagBarInstance.resize();
    }

    onUnmounted(() => {
      userTrendInstance && userTrendInstance.dispose();
      categoryPieInstance && categoryPieInstance.dispose();
      tagBarInstance && tagBarInstance.dispose();
      window.removeEventListener('resize', resizeCharts);
    });

    return {
      userTrendChart,
      categoryPieChart,
      tagBarChart
    };
  }
};
</script>

<style scoped>
.statistics-container {
  padding: 32px 24px;
  background: #f5f6fa;
  min-height: 100vh;
}
.overview-cards {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}
.card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  padding: 24px 20px;
  min-width: 180px;
  transition: box-shadow 0.2s;
}
.card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}
.card-icon.user { background: #e3f2fd; }
.card-icon.article { background: #fce4ec; }
.card-icon.tag { background: #e8f5e9; }
.card-icon.admin { background: #fff3e0; }
.card-content {
  flex: 1;
}
.card-title {
  font-size: 15px;
  color: #888;
  margin-bottom: 6px;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #222;
}
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 32px;
}
.chart-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  padding: 18px 12px 8px 12px;
  min-height: 340px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.chart {
  width: 100%;
  height: 300px;
}
@media (max-width: 900px) {
  .overview-cards {
    flex-direction: column;
    gap: 16px;
  }
  .charts-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style> 