<template>
  <main class="site-content" :class="{ 'site-content--tabs': $route.meta.isTab }">
    <!-- 主入口标签页 s -->
    <el-tabs
      v-if="$route.meta.isTab"
      v-model="mainTabsActiveName"
      :closable="true"
      @tab-click="selectedTabHandle"
      @tab-remove="removeTabHandle">
      <el-tab-pane
        v-for="item in mainTabs"
        :key="item.name"
        :label="item.title"
        :name="item.name">
        <el-card :body-style="siteContentViewHeight">
          <iframe v-if="item.type === 'iframe'" :src="item.iframeUrl" width="100%" height="100%" frameborder="0" scrolling="yes"></iframe>
          <router-view v-else v-slot="{ Component }">
            <keep-alive>
              <component :is="Component" v-if="item.name === mainTabsActiveName" />
            </keep-alive>
          </router-view>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    <!-- 标签页工具下拉菜单 -->
    <el-dropdown v-if="$route.meta.isTab" class="site-tabs__tools" :show-timeout="0">
      <i class="el-icon-arrow-down el-icon--right"></i>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="tabsCloseCurrentHandle">关闭当前标签页</el-dropdown-item>
          <el-dropdown-item @click="tabsCloseOtherHandle">关闭其它标签页</el-dropdown-item>
          <el-dropdown-item @click="tabsCloseAllHandle">关闭全部标签页</el-dropdown-item>
          <el-dropdown-item @click="tabsRefreshCurrentHandle">刷新当前标签页</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <!-- 主入口标签页 e -->
    <el-card v-else :body-style="siteContentViewHeight">
      <router-view v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </el-card>
  </main>
</template>

<script>
  import { useCommonStore } from '@/stores/common'
  import { isURL } from '@/utils/validate'
  
  export default {
    setup() {
      const commonStore = useCommonStore()
      
      return {
        commonStore
      }
    },
    data () {
      return {
      }
    },
    computed: {
      documentClientHeight: {
        get () { return this.commonStore.documentClientHeight }
      },
      menuActiveName: {
        get () { return this.commonStore.menuActiveName },
        set (val) { this.commonStore.updateMenuActiveName(val) }
      },
      mainTabs: {
        get () { return this.commonStore.mainTabs },
        set (val) { this.commonStore.updateMainTabs(val) }
      },
      mainTabsActiveName: {
        get () { return this.commonStore.mainTabsActiveName },
        set (val) { this.commonStore.updateMainTabsActiveName(val) }
      },
      siteContentViewHeight () {
        var height = this.documentClientHeight - 50 - 30 - 2
        if (this.$route.meta.isTab) {
          height -= 40
          return isURL(this.$route.meta.iframeUrl) ? { height: height + 'px' } : { minHeight: height + 'px' }
        }
        return { minHeight: height + 'px' }
      }
    },
    methods: {
      // tabs, 选中tab
      selectedTabHandle (tab) {
        // console.log(tab.name)
        tab = this.mainTabs.filter(item => item.name === tab.name)
        if (tab.length >= 1) {
          this.$router.push({ name: tab[0].name })
        }
      },
      // tabs, 删除tab
      removeTabHandle (tabName) {
        this.mainTabs = this.mainTabs.filter(item => item.name !== tabName)
        if (this.mainTabs.length >= 1) {
          // 当前选中tab被删除
          if (tabName === this.mainTabsActiveName) {
            this.$router.push({ name: this.mainTabs[this.mainTabs.length - 1].name }, () => {
              this.mainTabsActiveName = this.$route.name
            })
          }
        } else {
          this.menuActiveName = ''
          this.$router.push({ name: 'home' })
        }
      },
      // tabs, 关闭当前
      tabsCloseCurrentHandle () {
        this.removeTabHandle(this.mainTabsActiveName)
      },
      // tabs, 关闭其它
      tabsCloseOtherHandle () {
        this.mainTabs = this.mainTabs.filter(item => item.name === this.mainTabsActiveName)
      },
      // tabs, 关闭全部
      tabsCloseAllHandle () {
        this.mainTabs = []
        this.menuActiveName = ''
        this.$router.push({ name: 'home' })
      },
      // tabs, 刷新当前
      tabsRefreshCurrentHandle () {
        var tempTabName = this.mainTabsActiveName
        this.removeTabHandle(tempTabName)
        this.$nextTick(() => {
          this.$router.push({ name: tempTabName })
        })
      }
    }
  }
</script>

