# NSFConverter
A sample tool to convert Lotus Domino/Notes NSF files to MarkLogic documents

## �T�v
HTTP POST��Body�����ׂ�nsf�t�@�C������Notes�����𒊏o����MarkLogic�̃h�L�������g�Ƃ��ēo�^���܂��B
MarkLogic�̃t�B���^��.nsf�Ή�搂��Ă���̂ł����A�R���e���c�^�C�v��������Ă���Ȃ��悤�Ȃ̂�(�v�d�l�m�F)�n���܂����B

## �K�v�Ȃ���
- Lotus Notes ���o���h����jvm��jar���g���܂��B�ϊ��p�T�[�o�Ƃ��ė��p���镪�̃��C�Z���X�����͕K�v�ɂȂ邩�ƁB
- XCC/J ��MarkLogic�Ђ̃T�C�g����GET���܂��B
- Java�A�v���P�[�V�����T�[�o ��Tomcat�œ���m�F���Ă��܂��B�N���X�p�X�ݒ肠����� tomcat-bin-sample �ȉ���������������������B

## �C���X�g�[��
1. �\�[�X��1.5�݊��ŃR���p�C�����Ă��������B�ŐV�o�[�W�����ŃR���p�C�������Notes�o���h����jvm�ɂ͂�����܂��B
1. class�t�@�C���� webapp/WEB-INF/classes(/com/github/ytsejam5/nsfconverter) �ȉ��ɒu���܂��B
1. web.xml��MarkLogic�Ƃ̐ڑ��ݒ肪����܂��̂ŁA���p�ӂ��Ă���XDBC�A�v���P�[�V�����T�[�o�Ƃ̐ڑ��ݒ�𖄂߂Ă��������B
1. webapp���f�v���C
�œ����͂��ł��B���s�����Ă��珑�������܂��B�B

## �g����
- MarkLogic��CPF�����肩�火�ŌĂяo���ƍK���ɂȂ��Ǝv���܂��B
```
let $converter-url := "http://${�R���o�[�^�̃z�X�g}:${�|�[�g}/nsfconveter"
let $document := fn:doc($cpf:document-uri)
let $filtered-data := xdmp:document-filter($document)
let $content-type := $filtered-data/*:head/*:meta[@name eq "content-type"]/@content
return
	if ($content-type eq "application/vnd.lotus-wordpro") then (
		xdmp:http-post($converter-url, (()), ($document/node()))
	) else ()
```

- curl���火�ł��o�^�ł��܂��B
```
curl -X GET --data-binary @test.nsf http://localhost:58080/nsfconverter
```
