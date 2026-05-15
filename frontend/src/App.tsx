import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <div style={{ padding: 24 }}>
        <h1>Zong - 技术知识生产与自动化交付平台</h1>
        <p>前端脚手架已就绪，待开发业务页面。</p>
      </div>
    </ConfigProvider>
  )
}

export default App
