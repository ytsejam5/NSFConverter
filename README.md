# NSFConverter
A sample tool to convert Lotus Domino/Notes NSF files to MarkLogic documents

## 概要
HTTP POSTのBodyから喰べたnsfファイルからNotes文書を抽出してMarkLogicのドキュメントとして登録します。
MarkLogicのフィルタが.nsf対応謳っているのですが、コンテンツタイプしか取ってくれないようなので(要仕様確認)拵えました。

## 必要なもの
- Lotus Notes ※バンドルのjvmとjarを使います。変換用サーバとして利用する分のライセンスだけは必要になるかと。
- XCC/J ※MarkLogic社のサイトからGETします。
- Javaアプリケーションサーバ ※Tomcatで動作確認しています。クラスパス設定あたりは tomcat-bin-sample 以下あたりをご覧ください。

## インストール
1. ソースは1.5互換でコンパイルしてください。最新バージョンでコンパイルするとNotesバンドルのjvmにはじかれます。
1. classファイルを webapp/WEB-INF/classes(/com/github/ytsejam5/nsfconverter) 以下に置きます。
1. web.xmlにMarkLogicとの接続設定がありますので、ご用意しているXDBCアプリケーションサーバとの接続設定を埋めてください。
1. webappをデプロイ
で動くはずです。※不足してたら書き足します。。

## 使い方
- MarkLogicのCPFあたりから↓で呼び出すと幸せになれると思います。
```
let $converter-url := "http://${コンバータのホスト}:${ポート}/nsfconveter"
let $document := fn:doc($cpf:document-uri)
let $filtered-data := xdmp:document-filter($document)
let $content-type := $filtered-data/*:head/*:meta[@name eq "content-type"]/@content
return
	if ($content-type eq "application/vnd.lotus-wordpro") then (
		xdmp:http-post($converter-url, (()), ($document/node()))
	) else ()
```

- curlから↓でも登録できます。
```
curl -X GET --data-binary @test.nsf http://localhost:58080/nsfconverter
```
