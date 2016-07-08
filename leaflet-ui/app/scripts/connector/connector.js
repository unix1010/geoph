import Axios from 'axios';
import {API_BASE_URL}  from '../constants/constants';
import Settings from '../util/settings';
import {connect } from 'react-redux'
import Store from '../store/configureStore.js';
import Qs from 'qs';


console.log(Settings);

const POST= 'POST';
const GET= 'GET';
const PUT= 'PUT';
const DELETE= 'DELETE';

class Connector {


	setAuthToken(token){
		this.token=token;
	}

	getSecurityHeader(){
		return {'X-Security-token': this.token};
	}

	get(url, params = {}) {
		return new Promise(
			function(resolve, reject) { // (A)
				
				Axios.get(url, {
					responseType: 'json',
					params: params,
					paramsSerializer: function(params) {
						return Qs.stringify(params, {arrayFormat: 'repeat'})
					},

				})
				.then(function(response) {
					resolve(response);
				})
				.catch(function(response) {
					reject(response);
				});
			});
	}

	put(url, body = {}) {
		return new Promise(
			function(resolve, reject) {
				Axios.put(url, body)
				.then(function(response) {
					resolve(response);
				})
				.catch(function(response) {
					reject(response);
				});
			});
	}


	delete(url) {
		return new Promise(
			function(resolve, reject) {
				Axios.delete(url)
				.then(function(response) {
					resolve(response);
				})
				.catch(function(response) {
					reject(response);
				});
			});
	}




	post(url, data = {},config={}) {
		return new Promise(
			function(resolve, reject) {
				Axios.post(url, data,config)
				.then(function(response) {
					resolve(response);
				})
				.catch(function(response) {
					reject(response);
				});
			});
	}

	/*A method should always return a promise*/
	call(verb,endpoint, params , config) {

		let apiRoot = Settings.get('API',API_BASE_URL);
		let url = `${apiRoot}${endpoint}`; 


		let caller;
		if (verb == GET) caller = this.get;
		if (verb == POST) caller = this.post;
		if (verb == PUT ) caller = this.put;
		if (verb == DELETE ) caller = this.delete;


		return new Promise((resolve, reject) => {
			caller(url, params,config).then((response) => {
				resolve(response.data);
			}).catch((err) => {
				console.log('Error when trying to get backend data')
				reject(err);
			})
		})
	}

	/**/
	loadLayerByOptions(options,params={}) {

		return new Promise( (resolve, reject) => {
			
			let url=Settings.get('API',options.ep);
			const {level,quality} = options.settings;
			const {id, filters,indicator_id}=options;

			if (level){
				url=url.replace('${level}',level);
			}
			if (indicator_id){
				url=url.replace('${indicator_id}',indicator_id);
			}
			if (quality){
				Object.assign(params,{quality})
			}
			Object.assign(params, filters)
			
			this.call(GET,url, params).then((data) => {
				/*apply any data transformation*/
				
				resolve({id,data}); ////resolve with original data or perform any data transformation needed

			}).catch(reject)
		});
	}



	getFilterData(filterType) {
		return new Promise( (resolve, reject) => {
			let path = Settings.get('API','FILTER_LIST')[filterType];
			if (path.mock) {
				this.call(GET,path.path, {}, true).then((data) => {
					resolve(data); ////resolve with original data or perform any data transformation needed			
				}).catch(reject)
			} else {
				this.call(GET, path, {}).then((data) => {
					resolve(data); ////resolve with original data or perform any data transformation needed			
				}).catch(reject)
			}
		});
	}

	getChartData(filters) {
		return new Promise( (resolve, reject) => {
			let path = Settings.get('API','CHARTS');
			if (path.mock) {
				this.call(GET,path.path, {}, true).then((data) => {
					resolve(data);		
				}).catch(reject)
			} else {
				this.call(GET, path, filters).then((data) => {
					resolve(data); 	
				}).catch(reject)
			}
		});
	}


	login(options){
		let apiRoot = Settings.get('API',API_BASE_URL);
		let endpoint = Settings.get('API','LOGIN');
		let url = `${apiRoot}${endpoint}`; 

		return new Promise( (resolve, reject) => {
			const {username,password} = options;
		
			this.post(url, {username:username,password:password}).then((response) => {
				console.log(response.headers["x-security-token"]);
				this.setAuthToken(response.headers["x-security-token"]) ;
				resolve(response.data);	
			})
			.catch((error)=>{
				reject(error);	
			})
		})
	}

	logout(){

	}


	getIndicatorList(){
		return this.call(GET,Settings.get('API','INDICATOR_LIST'),{});
	}

	removeIndicator(id){
		let url=Settings.get('API','INDICATOR');
		url=url.replace('${id}',id);
		return this.call(DELETE,url,{});
	}

	uploadIndicator(options){
		const URL=Settings.get('API',API_BASE_URL) + Settings.get('API','INDICATOR_UPLOAD');
		return new Promise( (resolve, reject) => {
			const {file,name,template,css} = options;

			let url = Settings.get('API','INDICATOR_UPLOAD');
			var data = new FormData();
			data.append('name', name);
			data.append('admLevel', template);
			data.append('colorScheme', css);
			data.append('file',file);
			this.call(POST,url,data,{ headers: this.getSecurityHeader()}).then(resolve).catch(reject);
		})
	}


	getProjectPopupData(filters, tab) {
		return new Promise( (resolve, reject) => {
			let path = Settings.get('API','PROJECT_POPUP')[tab];
			this.call(GET, path, filters).then((data) => {
				resolve(data); 	
			}).catch(reject)
		});
	}

	getProjectsWithFilters(filters) {
		return new Promise( (resolve, reject) => {
			let path = Settings.get('API','PROJECT_LIST');
			this.call(GET, path, filters).then((data) => {
				resolve(data); 	
			}).catch(reject)
		});
	}

	getStats(filters) {
		return new Promise( (resolve, reject) => {
			let path = Settings.get('API','STATS');
			this.call(GET, path, filters).then((data) => {
				resolve(data); 	
			}).catch(reject)
		});

	}


	saveMap(dataToSave) {
		return new Promise( (resolve, reject) => {
			let path = Settings.get('API','SAVE');
			console.log("---saveMap connector---");
			this.call(POST, path, dataToSave).then((data) => {
				resolve(data); 	
			}).catch(reject)
		});
	}
}


if (!window.__connector){ //singleton connector 
	window.__connector=new Connector();
}
export default window.__connector;
