require 'json_spec'
require 'spec_helper'
require 'exec_command.rb'
require 'usecase_reader.rb'
require 'translate_sample_reader.rb'
require 'faraday'
require 'pfcli'

def conn 
  Faraday.new(:url => 'https://api.openbd.jp') do |builder|
    builder.adapter(Faraday.default_adapter)
  end
end

def _get(r)
  res = conn.get do |req|
    req.url(r.url)
#   req.params = r.params
    req.headers = r.headers
  end
  return Pfcli::Response.new(body_s: res.body, status: res.status, headers: res.headers)
end

describe 'First test: v2_api_spec' do
    let(:url) do '/v1/get?isbn=9780001971714&pretty' end
    let(:params) do { 'param01': 'abc', 'param02': 'DEF' } end
    let(:headers) do { 'Header01': '123', 'Header02': '456' } end
    let(:instance) do
        instance = Pfcli::HttpClient.new
    end

    before do
        req = Pfcli::Request.new(url: url, params: params, headers: headers)
        @res = _get(req)
        @result = JSON.parse(@res.body_s)
    end

    it 'test' do
        body = @res.body_s
        expect(@res.status).to eq(200)
        expect(@res.headers['alt-svc']).to eq('clear')
        expect(@result[0]).to have_key('onix')
        expect(@result[0]).to have_key('summary')

    end
end

describe '基本的なパスやパラメーターのチェック処理の確認' do
    context '200 OK test' do
        let(:url) do '/v1/get?isbn=9780002161992&pretty' end
        let(:dids) do [ 'did001', 'did002', 'did003', 'did004' ] end
        let(:tmids) do [ 'tmid001', 'tmid002', 'tmid003', 'tmid004' ] end
        let(:params) do { 'subscription-key': 'abc', 'dids': dids, 'tmids': tmids } end
        let(:headers) do { 'Header01': '123', 'Header02': '456' } end
        let(:instance) do
            instance = Pfcli::HttpClient.new
        end
    
        #request body
        let(:requestId) do '1234567890' end
        let(:uid) do '1234567890' end
        let(:langFrom) do 'ja' end
        let(:langTo) do 'fr' end
        let(:langInterm) do 'en' end
        let(:label) do 'label' end
        let(:profile) do 'profile' end
        let(:req_body) do 
            { 'requestId': requestId, 'uid': uid, 'langFrom': langFrom, 'langTo': langTo, 'label': label, 'profile': profile, 'langInterm': langInterm } 
        end
        before do
            req = Pfcli::Request.new(url: url, params: params, headers: headers, body_s: req_body)
            @res = _get(req)
            @result = JSON.parse(@res.body_s)
        end
    
        it 'test' do
            body = @res.body_s
            expect(@res.status).to eq(200)
            expect(@res.headers['alt-svc']).to eq('clear')
            expect(@result[0]).to have_key('onix')
            expect(@result[0]).to have_key('summary')
            expect(@result[0]['onix']).to have_key('DescriptiveDetail')
        end
    end


end

describe '入力データチェック' do
    describe 'Query parameter check' do
        let(:url) do '/v1/get?isbn=9780002112925&pretty' end
        let(:dids) do [ 'did001', 'did002', 'did003', 'did004' ] end
        let(:tmids) do [ 'tmid001', 'tmid002', 'tmid003', 'tmid004' ] end
        let(:params) do { 'subscription-key': 'abc', 'dids': dids, 'tmids': tmids } end
        let(:headers) do { 'Header01': '123', 'Header02': '456' } end
        let(:instance) do
            instance = Pfcli::HttpClient.new
        end

        #request body
        let(:requestId) do '1234567890' end
        let(:uid) do '1234567890' end
        let(:langFrom) do 'ja' end
        let(:langTo) do 'fr' end
        let(:langInterm) do 'en' end
        let(:label) do 'label' end
        let(:profile) do 'profile' end
        let(:req_body) do 
            { 'requestId': requestId, 'uid': uid, 'langFrom': langFrom, 'langTo': langTo, 'label': label, 'profile': profile, 'langInterm': langInterm } 
        end

        req_names = ['requestId', 'uid', 'langFrom', 'langTo', 'label', 'profile']
        req_names.each do |param|
            context "'#{param}' check" do
                it 'test' do
                    qs = {}
                    req_body.each do |key, val| 
                        if param != key.to_s then
                            qs.store(key, val)
                        end
                    end

#                   expect(qs).to eq(req_body)
                    req = Pfcli::Request.new(url: url, params: params, headers: headers, body_s: req_body)
                    @res = _get(req)
                    @result = JSON.parse(@res.body_s)

                    expect(@res.status).to eq(200)
                    expect(@result['response']).to have_key('reason')
                end
            end
        end

    end
end
