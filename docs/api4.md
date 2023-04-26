
<details>

# <summary> aycController: </summary>

## <summary> 添加数据接口 </summary>

<code>

```
httpMethods: POST  
httpUrls: /insertAycList

```

### 参数

|类型|名称|说明|是否必需|
|---|---|---|---|
|**String**|**name**|    | false |
|**Integer**|**age**|    | false |


### HTTP请求示例

#### 请求 header
```

authorization : Bearer xxxxx
Content-Type : application/json

```

#### 请求 body
```
{
 name:xxxxxxx,
 age:1,
}
```


### HTTP响应示例

#### 响应 body
```
{
 total:1,
 code:1,
 message:xxxxxxx,
 successful:true,
}
```

</code>
</details>

