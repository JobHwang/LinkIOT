/**
 * LinkIOT Dashboard
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional }                      from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams,
         HttpResponse, HttpEvent }                           from '@angular/common/http';
import { CustomHttpUrlEncodingCodec }                        from '../encoder';

import { Observable }                                        from 'rxjs/Observable';

import { Body } from '../model/body';
import { Body1 } from '../model/body1';
import { Body2 } from '../model/body2';
import { Body3 } from '../model/body3';
import { Body4 } from '../model/body4';
import { Body5 } from '../model/body5';
import { Data } from '../model/data';
import { Device } from '../model/device';
import { DeviceState } from '../model/deviceState';
import { InlineResponse200 } from '../model/inlineResponse200';
import { Sensor } from '../model/sensor';
import { SimpleStatus } from '../model/simpleStatus';

import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';


@Injectable()
export class DefaultService {

    protected basePath = '/v1';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();

    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
        if (basePath) {
            this.basePath = basePath;
        }
        if (configuration) {
            this.configuration = configuration;
            this.basePath = basePath || configuration.basePath || this.basePath;
        }
    }

    /**
     * @param consumes string[] mime-types
     * @return true: consumes contains 'multipart/form-data', false: otherwise
     */
    private canConsumeForm(consumes: string[]): boolean {
        const form = 'multipart/form-data';
        for (const consume of consumes) {
            if (form === consume) {
                return true;
            }
        }
        return false;
    }


    /**
     * 删除用户
     * 
     * @param username 用户名
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public delUser(username?: string, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public delUser(username?: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public delUser(username?: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public delUser(username?: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (username !== undefined && username !== null) {
            queryParameters = queryParameters.set('username', <any>username);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.delete(`${this.basePath}/user`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 删除设备
     * 
     * @param deviceId 设备ID
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public deleteDevice(deviceId: string, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public deleteDevice(deviceId: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public deleteDevice(deviceId: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public deleteDevice(deviceId: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling deleteDevice.');
        }

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (deviceId !== undefined && deviceId !== null) {
            queryParameters = queryParameters.set('deviceId', <any>deviceId);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.delete(`${this.basePath}/db/devices`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 删除传感器
     * 
     * @param deviceId 设备ID
     * @param sensorId 传感器ID
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public deleteSensor(deviceId: string, sensorId: number, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public deleteSensor(deviceId: string, sensorId: number, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public deleteSensor(deviceId: string, sensorId: number, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public deleteSensor(deviceId: string, sensorId: number, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling deleteSensor.');
        }
        if (sensorId === null || sensorId === undefined) {
            throw new Error('Required parameter sensorId was null or undefined when calling deleteSensor.');
        }

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (sensorId !== undefined && sensorId !== null) {
            queryParameters = queryParameters.set('sensorId', <any>sensorId);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.delete(`${this.basePath}/db/sensors/${encodeURIComponent(String(deviceId))}`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 强制下线设备
     * 
     * @param deviceId 设备ID
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public forceClose(deviceId: string, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public forceClose(deviceId: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public forceClose(deviceId: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public forceClose(deviceId: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling forceClose.');
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/db/device/close/${encodeURIComponent(String(deviceId))}`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 获取传感器数据
     * 
     * @param deviceId 设备ID
     * @param sensorId 传感器ID
     * @param offset 数据列表起始位置
     * @param limit 数据列表记录数
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getData(deviceId: string, sensorId: string, offset: number, limit: number, observe?: 'body', reportProgress?: boolean): Observable<Array<Data>>;
    public getData(deviceId: string, sensorId: string, offset: number, limit: number, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<Data>>>;
    public getData(deviceId: string, sensorId: string, offset: number, limit: number, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<Data>>>;
    public getData(deviceId: string, sensorId: string, offset: number, limit: number, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling getData.');
        }
        if (sensorId === null || sensorId === undefined) {
            throw new Error('Required parameter sensorId was null or undefined when calling getData.');
        }
        if (offset === null || offset === undefined) {
            throw new Error('Required parameter offset was null or undefined when calling getData.');
        }
        if (limit === null || limit === undefined) {
            throw new Error('Required parameter limit was null or undefined when calling getData.');
        }

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (offset !== undefined && offset !== null) {
            queryParameters = queryParameters.set('offset', <any>offset);
        }
        if (limit !== undefined && limit !== null) {
            queryParameters = queryParameters.set('limit', <any>limit);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/db/sensor/data/${encodeURIComponent(String(deviceId))}/${encodeURIComponent(String(sensorId))}`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 用户登录
     * 
     * @param user 用户名
     * @param pass 密码
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getLogin(user: string, pass: string, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public getLogin(user: string, pass: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public getLogin(user: string, pass: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public getLogin(user: string, pass: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (user === null || user === undefined) {
            throw new Error('Required parameter user was null or undefined when calling getLogin.');
        }
        if (pass === null || pass === undefined) {
            throw new Error('Required parameter pass was null or undefined when calling getLogin.');
        }

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (user !== undefined && user !== null) {
            queryParameters = queryParameters.set('user', <any>user);
        }
        if (pass !== undefined && pass !== null) {
            queryParameters = queryParameters.set('pass', <any>pass);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/auth/login`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 用户登出
     * 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getLogout(observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public getLogout(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public getLogout(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public getLogout(observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/auth/logout`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 某一个设备的传感器列表
     * 
     * @param deviceId 设备ID
     * @param offset 列表起始位置
     * @param limit 记录数量
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getSensors(deviceId: string, offset: number, limit: number, observe?: 'body', reportProgress?: boolean): Observable<Array<Sensor>>;
    public getSensors(deviceId: string, offset: number, limit: number, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<Sensor>>>;
    public getSensors(deviceId: string, offset: number, limit: number, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<Sensor>>>;
    public getSensors(deviceId: string, offset: number, limit: number, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling getSensors.');
        }
        if (offset === null || offset === undefined) {
            throw new Error('Required parameter offset was null or undefined when calling getSensors.');
        }
        if (limit === null || limit === undefined) {
            throw new Error('Required parameter limit was null or undefined when calling getSensors.');
        }

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (offset !== undefined && offset !== null) {
            queryParameters = queryParameters.set('offset', <any>offset);
        }
        if (limit !== undefined && limit !== null) {
            queryParameters = queryParameters.set('limit', <any>limit);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/db/sensors/${encodeURIComponent(String(deviceId))}`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 获取当前设备状态
     * 
     * @param deviceId 设备ID
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getState(deviceId: string, observe?: 'body', reportProgress?: boolean): Observable<DeviceState>;
    public getState(deviceId: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<DeviceState>>;
    public getState(deviceId: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<DeviceState>>;
    public getState(deviceId: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling getState.');
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/db/device/state/${encodeURIComponent(String(deviceId))}`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 查询用户信息
     * 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getUser(observe?: 'body', reportProgress?: boolean): Observable<InlineResponse200>;
    public getUser(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<InlineResponse200>>;
    public getUser(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<InlineResponse200>>;
    public getUser(observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/user`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 获取用户创建的所有设备
     * 
     * @param offset 起始列表位置
     * @param limit 本次列表数量
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public listDevices(offset?: number, limit?: number, observe?: 'body', reportProgress?: boolean): Observable<Array<Device>>;
    public listDevices(offset?: number, limit?: number, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<Device>>>;
    public listDevices(offset?: number, limit?: number, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<Device>>>;
    public listDevices(offset?: number, limit?: number, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (offset !== undefined && offset !== null) {
            queryParameters = queryParameters.set('offset', <any>offset);
        }
        if (limit !== undefined && limit !== null) {
            queryParameters = queryParameters.set('limit', <any>limit);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.get(`${this.basePath}/db/devices`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 更新设备
     * 
     * @param body 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postDevice(body?: Body3, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public postDevice(body?: Body3, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public postDevice(body?: Body3, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public postDevice(body?: Body3, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.post(`${this.basePath}/db/devices`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 更新传感器信息
     * 
     * @param deviceId 设备ID
     * @param body 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postSensor(deviceId: string, body?: Body5, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public postSensor(deviceId: string, body?: Body5, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public postSensor(deviceId: string, body?: Body5, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public postSensor(deviceId: string, body?: Body5, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling postSensor.');
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.post(`${this.basePath}/db/sensors/${encodeURIComponent(String(deviceId))}`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 用户设置新的设备状态
     * 
     * @param body 用户设置的新状态
     * @param deviceId 设备ID
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postState(body: any, deviceId: string, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public postState(body: any, deviceId: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public postState(body: any, deviceId: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public postState(body: any, deviceId: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (body === null || body === undefined) {
            throw new Error('Required parameter body was null or undefined when calling postState.');
        }
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling postState.');
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.post(`${this.basePath}/db/device/state/${encodeURIComponent(String(deviceId))}`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 修改用户信息
     * 
     * @param body 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public postUser(body?: Body1, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public postUser(body?: Body1, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public postUser(body?: Body1, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public postUser(body?: Body1, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.post(`${this.basePath}/user`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 创建新的设备
     * 
     * @param body 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public putDevice(body?: Body2, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public putDevice(body?: Body2, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public putDevice(body?: Body2, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public putDevice(body?: Body2, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.put(`${this.basePath}/db/devices`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 创建传感器
     * 
     * @param deviceId 设备ID
     * @param body 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public putSensor(deviceId: string, body?: Body4, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public putSensor(deviceId: string, body?: Body4, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public putSensor(deviceId: string, body?: Body4, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public putSensor(deviceId: string, body?: Body4, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
        if (deviceId === null || deviceId === undefined) {
            throw new Error('Required parameter deviceId was null or undefined when calling putSensor.');
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.put(`${this.basePath}/db/sensors/${encodeURIComponent(String(deviceId))}`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * 用户注册
     * 
     * @param body 
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public putUser(body?: Body, observe?: 'body', reportProgress?: boolean): Observable<SimpleStatus>;
    public putUser(body?: Body, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SimpleStatus>>;
    public putUser(body?: Body, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SimpleStatus>>;
    public putUser(body?: Body, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected != undefined) {
            headers = headers.set('Content-Type', httpContentTypeSelected);
        }

        return this.httpClient.put(`${this.basePath}/user`,
            {
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }
