### 3.8 자바 데이터 전송 (serializable)
- 데이터 전송은 결국 byte[] 데이터를 주고 받는 기능인데, 고객사에게 고성능 전송데이터를 원해서 관련된 내용을 정리하였다.
- 기본적인 java serializable, json 데이터 부터 구글에서 만든 proto buffer, sbe(simple binary encoding)까지 정리한다.

#### 3.8.1 java serializable
- java.io.Serializable interface를 구현한 object 를 object stream 기술을 사용하여 stream of bytes 로 변환해서 사용
- serialization & deserialization 하는 곳에 동일한 object 객체가 필요하다.

#### 3.8.2 json
- 데이터를 저장하거나 전송할 때 많이 사용되는 경량의 DATA 교환 형식
- xml, csv 등이 있지만 이미 json으로 통일된 현 시점에 다른 형식은 고려할 필요가 없음
- java 에서 주로 사용되는 json parser 기술로는 spring 에 defatult 로 사용하는 jackson lib 와 구글에서 만든 gson 기술이 존재

#### 3.8.3 proto buffer
- 통신 프로토콜, 데이터 저장소 등에 사용하기 위해 구조화된 데이터를 직렬화하는 언어 중립적이고 플랫폼 중립적인 확장 가능한 메커니즘
- 데이터 구조를 정의한 다음에 serialization & deserialization 를 통해 스템 간에 데이터를 이식하는 방식
- stub 파일 방식: 특정하게 정의에 문서에 따라 자동으로 생성되는 소스를 활용하는 방식

```protobuf
syntax = "proto3";
package theater;
option java_package = "stn.serialization.protobuf";

message Theater {
    Header header = 1;
    int64 blockIdx = 2;
    int64 progNum = 3;
    string betMode = 4;
    int64 poolStake = 5;
    int64 matchCnt = 6;
    repeated Game games = 7;
}

message Game{
    int64 matchNo = 1;
    int64 matchSel = 2;
    int64 matchOdds = 3;
    int64 matchHandi = 4;
}

message Header{
    string startCode = 1;
    string transType = 2;
    int64 transLen = 3;
    string transId = 4;
    int64 tkserNum = 5;
    bool resyncYn = 6;
    string resultCd = 7;
    string tagNum = 8;
    string gameId = 9;
    int64 totalStake = 10;
    int64 gcBlockCnt = 11;
}
```

#### 3.8.4 simple binary encoding(SBE)
- xml 형태의 데이터 구조를 정의한 후 이를 통해 소스를 제공하고 이를 통해 직렬화 하는 오픈 소스

```xml
<?xml version="1.0" encoding="UTF-8"?>
<sbe:messageSchema xmlns:sbe="http://fixprotocol.io/2016/sbe"
                   package="com.baeldung.sbe.stub" id="1" version="0" semanticVersion="5.2"
                   description="A schema represents stock market data.">
    <types>
        <composite name="messageHeader" description="Message identifiers and length of message root">
            <type name="blockLength" primitiveType="uint16"/>
            <type name="templateId" primitiveType="uint16"/>
            <type name="schemaId" primitiveType="uint16"/>
            <type name="version" primitiveType="uint16"/>
        </composite>
        <composite name="varStringEncoding">
            <type name="length" primitiveType="uint32" maxValue="1073741824"/>
            <type name="varData" primitiveType="uint8" length="0" characterEncoding="UTF-8"/>
        </composite>
        <composite name="groupSizeEncoding" description="Repeating group dimensions.">
            <type name="blockLength" primitiveType="uint16"/>
            <type name="numInGroup" primitiveType="uint16"/>
        </composite>

        <type name="string2" primitiveType="char" length="2" characterEncoding="ASCII"/>
        <type name="string3" primitiveType="char" length="3" characterEncoding="ASCII"/>
        <type name="string13" primitiveType="char" length="13" characterEncoding="ASCII"/>
        <type name="string16" primitiveType="char" length="16" characterEncoding="ASCII"/>
        <type name="_int8" primitiveType="int8"/>
        <type name="_int16" primitiveType="int16"/>
        <type name="_int32" primitiveType="int32"/>
        <type name="_int64" primitiveType="int64"/>
        <composite name="Header" description="A quote represents the price of a stock in a market">
            <ref name="startCode" type="string3"/>
            <ref name="transType" type="string3"/>
            <ref name="transLen" type="_int16"/>
            <ref name="transId" type="string16"/>
            <ref name="tkserNum" type="_int64"/>
            <ref name="resyncYn" type="_int16"/>
            <ref name="resultCd" type="string2"/>
            <ref name="tagNum" type="string13"/>
            <ref name="gameId" type="string2"/>
            <ref name="totalStake" type="_int16"/>
            <ref name="gcBlockCnt" type="_int16"/>
        </composite>
    </types>
    <sbe:message name="SampleGroup" id="1" description="Sample with group">
        <field name="header" id="2" type="Header"/>
        <field name="blockIdx" id="3" type="int16"/>
        <field name="progNum" id="4" type="int16"/>
        <field name="betMode" id="5" type="string2"/>
        <field name="poolStake" id="6" type="int16"/>
        <field name="matchCnt" id="7" type="int16"/>
        <group name="group" id="10" dimensionType="groupSizeEncoding">
            <field name="matchNo" id="11" type="uint16"/>
            <field name="matchSel" id="12" type="uint16"/>
            <field name="matchOdds" id="13" type="uint16"/>
            <field name="matchHandi" id="14" type="uint16"/>
        </group>
    </sbe:message>
</sbe:messageSchema>
```


#### 3.8.5 성능 비교 
- 테스트 방식 : 1_000_000 건의 가상의 데이터 (BTS-RM 사이 내부 통신에서 사용되는 데이터구조) 를 생성하여 serialization 하여 byte 형태의 데이터를 다시 deserialization 하는 사이즈와 시간을 측정함
- 테스트 대상 : java serialization, json(jackson lib), json(gson), sbe(simple binary encording), proto buffer(google)

#### 3.8.5.1 1차 테스트

| serialization| 속도| byte 사이즈| 메모리 사용량|
| --| --| --| --|
| 자바 serialization| 19.741 s| 780 byte| 761 mb|
| ProtoBuffer| 2.166 s| 113 byte| 875 mb|
| gson(json)| 14.062 s| 779 byte| 1,001 mb|
| sbe| 1.100 s| 256 byte| 384 mb|
| jackson(json)| 6.341 s| 779 byte| 183 mb|


#### 3.8.5.2 2차 테스트

| serialization| 속도| byte 사이즈| 메모리 사용량|
| --| --| --| --| 
| 자바 serialization| 20.968 s| 780 byte| 1,192 mb|
| ProtoBuffer| 2.447 s| 113 byte| 1,074 mb|
| gson(json)| 14.766 s| 780 byte| 973 mb|
| sbe| 1.157 s| 256 byte| 383 mb|
| jackson(json)| 6.730 s| 780 byte| 87 mb|


#### 3.8.5.3 3차 테스트

| serialization| 속도| byte 사이즈| 메모리 사용량|
| --| --| --| --| 
| 자바 serialization| 21.013 s| 780 byte| 1,002 mb|
| ProtoBuffer| 2.547 s| 113 byte| 1,585 mb|
| gson(json)| 14.780 s| 780 byte| 1,037 mb|
| sbe| 1.089 s| 256 byte| 387 mb|
| jackson(json)| 6.706 s| 780 byte| 283 mb|


#### 3.8.5.4 4차 테스트

| serialization| 속도| byte 사이즈| 메모리 사용량|
| --| --| --| --| 
| 자바 serialization| 20.54 s| 780 byte| 1,155 mb|
| ProtoBuffer| 2.228 s| 113 byte| 875 mb|
| gson(json)| 14.725 s| 779 byte| 999 mb|
| sbe| 1.123 s| 256 byte| 384 mb|
| jackson(json)| 6.565 s| 779 byte| 308 mb|


#### 3.8.5.5 5차 테스트

| serialization| 속도| byte 사이즈| 메모리 사용량|
| --| --| --| --| 
| 자바 serialization| 21.534 s| 780 byte| 998 mb|
| ProtoBuffer| 2.689 s| 113 byte| 23 mb|
| gson(json)| 14.743 s| 780 byte| 1,034 mb|
| sbe| 1.120 s| 256 byte| 388 mb|
| jackson(json)| 6.413 s| 780 byte| 271 mb|
