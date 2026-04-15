<template>
  <div class="tunnel-template-list">
    <div class="card">
      <el-card class="template-card" shadow="never">
        <template #header>
          <div class="template-card__header">
            <span class="template-card__title">模板列表</span>
            <div class="action-buttons">
              <el-button :icon="Refresh" @click="loadList">刷新</el-button>
            </div>
          </div>
        </template>

        <!-- <el-alert class="template-tip" type="info" :closable="false" show-icon>
          <template #title>说明</template>
          <span>
            数据来自接口 <code>/tunnel/param/template/list</code>。可查看模板详情；有权限时可进入模板编辑页修改名称、状态、备注及各方向快照 JSON（编辑不影响已存在的隧道，仅改模板库）。
          </span>
        </el-alert> -->

        <div class="table-wrap">
          <el-table :data="list" v-loading="loading" stripe style="width: 100%" row-key="id">
            <el-table-column prop="templateName" label="模板名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="88">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
            <el-table-column label="创建时间" width="168">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime, false) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openDetail(row)">查看详情</el-button>
                <el-button v-if="canTunnelUpdate" type="primary" link @click="goEdit(row)">编辑</el-button>
                <el-button v-if="canTunnelUpdate" type="danger" link @click="onDelete(row)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无模板" />
            </template>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTunnelParamTemplateList, deleteTunnelParamTemplate } from '@/api/tunnel'
import { setTemplateParamContextId } from '@/utils/templateParamContext'
import { formatDateTime } from '@/utils/datetime'
import { hasPermission } from '@/utils/permission'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const canTunnelUpdate = computed(() => hasPermission('system:tunnel:update'))

const statusText = (s) => {
  if (s === 0) return '草稿'
  if (s === 1) return '启用'
  if (s === 2) return '停用'
  return '—'
}

const statusTagType = (s) => {
  if (s === 1) return 'success'
  if (s === 0) return 'info'
  if (s === 2) return 'danger'
  return 'info'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getTunnelParamTemplateList()
    if (res.code === 200) {
      list.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openDetail = (row) => {
  if (!row?.id) return
  setTemplateParamContextId(row.id)
  router.push('/tunnel/param/detail')
}

const goEdit = (row) => {
  if (!row?.id) return
  setTemplateParamContextId(row.id)
  router.push('/tunnel/param/edit')
}

const onDelete = async (row) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确定逻辑删除模板「${row.templateName || row.id}」？删除后列表不再展示。`, '删除模板', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    const res = await deleteTunnelParamTemplate(row.id)
    if (res.code === 200) {
      ElMessage.success(res.msg || '已删除')
      await loadList()
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadList()
})
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

  .template-tip {
    margin-bottom: 16px;
    line-height: 1.6;

    code {
      font-size: 12px;
      padding: 1px 6px;
      border-radius: 4px;
      background: rgba(0, 0, 0, 0.06);
    }
  }

  .table-wrap {
    min-height: 240px;
  }
}

</style>
