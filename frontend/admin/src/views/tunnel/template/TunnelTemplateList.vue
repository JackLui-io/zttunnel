<template>
  <div class="tunnel-template-list">
    <div class="card">
      <el-card class="template-card" shadow="never">
        <template #header>
          <div class="template-card__header">
            <span class="template-card__title">隧道模板</span>
            <div class="action-buttons">
              <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
              <el-tooltip
                content="后续开放：选择模板一键创建隧道，效果类似隧道列表中的「复制隧道群」。"
                placement="bottom"
              >
                <span class="action-buttons__hint-wrap">
                  <el-button type="primary" @click="onCreateFromTemplate">从模板新建</el-button>
                </span>
              </el-tooltip>
            </div>
          </div>
        </template>

        <el-alert
          class="template-tip"
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>说明</template>
          <span>
            本页用于集中管理隧道模板列表。后续支持在隧道列表将某隧道群「存为模板」，以及在本页「从模板新建」一键生成结构（与现有「复制」能力类似）。当前数据区待接口对接。
          </span>
        </el-alert>

        <div class="table-wrap">
          <el-table
            :data="list"
            v-loading="loading"
            stripe
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="templateName" label="模板名称" min-width="160" />
            <el-table-column prop="sourceTunnelName" label="来源隧道" min-width="160" />
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default>
                <el-button type="primary" link @click="onApplyTemplate">套用模板</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无隧道模板" />
            </template>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])

const handleRefresh = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 200))
    // 接口就绪后：加载模板列表
  } finally {
    loading.value = false
  }
}

function onCreateFromTemplate() {
  ElMessage.info('功能开发中模板新建入口尚未对接，后续与列表数据一并开放。')
}

function onApplyTemplate() {
  ElMessage.info('套用模板功能开发中，敬请期待。')
}
</script>

<style lang="scss" scoped>
.tunnel-template-list {
  .template-card {
    border: 1px solid rgba(0, 234, 255, 0.25);
    background: rgba(0, 234, 255, 0.03);
    border-radius: 8px;

    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid rgba(0, 234, 255, 0.15);
    }

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .template-card__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    flex-wrap: wrap;
  }

  .template-card__title {
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .action-buttons {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  .action-buttons__hint-wrap {
    display: inline-flex;
    vertical-align: middle;
  }

  .template-tip {
    margin-bottom: 16px;
    line-height: 1.6;
  }

  .table-wrap {
    min-height: 240px;
  }
}
</style>
