-- =====================================================================
-- auto-report-backend 索引初始化（OpenSearch，取代原 SQL DDL）
-- 目標：http://$OPENSEARCH_HOST:$OPENSEARCH_PORT（預設 192.168.28.132:9200）
-- 說明：應用啟動時 auto-create-index=true 也會自動建立，本腳本供手動初始化。
-- =====================================================================

-- 1) 異常設定（doc _id = bizName）
curl -X PUT "http://192.168.28.132:9200/exception_setting" -H "Content-Type: application/json" -d '
{
  "mappings": {
    "properties": {
      "bizName":     { "type": "keyword" },
      "autoProcess": { "type": "keyword" },
      "createdAt":   { "type": "date" },
      "updatedAt":   { "type": "date" }
    }
  }
}'

-- 2) 節假日設定（doc _id = holidayDate）
curl -X PUT "http://192.168.28.132:9200/holiday_setting" -H "Content-Type: application/json" -d '
{
  "mappings": {
    "properties": {
      "holidayDate": { "type": "date", "format": "yyyy-MM-dd" },
      "remark":      { "type": "keyword" },
      "createdAt":   { "type": "date" },
      "updatedAt":   { "type": "date" }
    }
  }
}'

-- 3) 配置字典（doc _id = type:code；type/code/label 為 text + keyword 子欄位，查詢請用 .keyword）
curl -X PUT "http://192.168.28.132:9200/config_dictionary" -H "Content-Type: application/json" -d '
{
  "mappings": {
    "properties": {
      "type":  { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
      "code":  { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
      "label": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
      "sort":  { "type": "integer" }
    }
  }
}'

-- 3-1) 配置字典種子資料（登入後前端載入）
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/bizName:MISF_LOGIN?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "bizName", "code": "MISF_LOGIN", "label": "行動投保登錄", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/processStatus:SUCCESS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "processStatus", "code": "SUCCESS", "label": "成功", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/processStatus:FAILED?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "processStatus", "code": "FAILED", "label": "失敗", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/processStatus:SKIPPED?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "processStatus", "code": "SKIPPED", "label": "未處理", "sort": 3 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRule:TOO_FREQUENT_LOGINS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRule", "code": "TOO_FREQUENT_LOGINS", "label": "密集登入", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRule:TOO_MANY_DAILY_LOGINS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRule", "code": "TOO_MANY_DAILY_LOGINS", "label": "單日登入次數異常", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRule:OFF_HOURS_LOGIN?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRule", "code": "OFF_HOURS_LOGIN", "label": "非上班時間登入", "sort": 3 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRuleRisk:TOO_FREQUENT_LOGINS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRuleRisk", "code": "TOO_FREQUENT_LOGINS", "label": "MEDIUM", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRuleRisk:TOO_MANY_DAILY_LOGINS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRuleRisk", "code": "TOO_MANY_DAILY_LOGINS", "label": "HIGH", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRuleRisk:OFF_HOURS_LOGIN?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRuleRisk", "code": "OFF_HOURS_LOGIN", "label": "LOW", "sort": 3 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRuleDetail:TOO_FREQUENT_LOGINS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRuleDetail", "code": "TOO_FREQUENT_LOGINS", "label": "同一帳號於 10 分鐘內登入達 5 次（含）以上。", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRuleDetail:TOO_MANY_DAILY_LOGINS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRuleDetail", "code": "TOO_MANY_DAILY_LOGINS", "label": "同一帳號單日登入次數達 20 次（含）以上。", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/matchedRuleDetail:OFF_HOURS_LOGIN?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "matchedRuleDetail", "code": "OFF_HOURS_LOGIN", "label": "於非上班時間（平日 22:00～06:00 或非上班日）登入。", "sort": 3 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/system_type:MISF?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "system_type", "code": "MISF", "label": "行動投保", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/system_type:ESERVICE_JD?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "system_type", "code": "ESERVICE_JD", "label": "經代系統", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/riskLevel:LOW?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "riskLevel", "code": "LOW", "label": "低", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/riskLevel:MEDIUM?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "riskLevel", "code": "MEDIUM", "label": "中", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/riskLevel:HIGH?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "riskLevel", "code": "HIGH", "label": "高", "sort": 3 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/eventStatus:PENDING?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "eventStatus", "code": "PENDING", "label": "待處理", "sort": 1 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/eventStatus:IN_PROGRESS?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "eventStatus", "code": "IN_PROGRESS", "label": "處理中", "sort": 2 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/eventStatus:TRACKING?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "eventStatus", "code": "TRACKING", "label": "持續追蹤", "sort": 3 }'
curl -X PUT "http://192.168.28.132:9200/config_dictionary/_doc/eventStatus:CLOSED?refresh=true" -H "Content-Type: application/json" -d '
{ "type": "eventStatus", "code": "CLOSED", "label": "已結案", "sort": 4 }'