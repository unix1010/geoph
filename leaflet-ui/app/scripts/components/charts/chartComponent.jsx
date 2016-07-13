import React from 'react';
import ReactDOM from 'react-dom';
import Plotly  from 'react-plotlyjs';
import { connect } from 'react-redux';
import { ButtonGroup, Button, Label } from 'react-bootstrap';
import * as Constants from '../../constants/constants';
import { parseDataChart } from '../../util/chartUtil';
import {formatValue} from '../../util/transactionUtil';
import translate from '../../util/translate';
require("./charts.scss");

export default class ChartComponent extends React.Component {

	constructor() {
	    super();
	    this.state = {'chartType': 'bar', 'measType': 'funding', 'hiddenlabels': []};
	}

	setChartType(type){
		this.props.onChangeType(this.props.chart, type);
	}

	setMeasType(type){
		this.props.onChangeMeasure(this.props.chart, type);
	}

	setItemsToShow(value){ 
		let val = this.props.chartData.itemsToShow;
		if (value=="less"){
			if (this.props.chartData.itemsToShow > Constants.CHART_ITEMS_STEP_AMOUNT){
				val = this.props.chartData.itemsToShow - Constants.CHART_ITEMS_STEP_AMOUNT;
			}	
		} else {
			if (this.props.chartData.itemsToShow < this.props.chartData.data.length){
				val = this.props.chartData.itemsToShow + Constants.CHART_ITEMS_STEP_AMOUNT;
			}
		}
		this.props.onChangeItemToShow(this.props.chart, val);
	}

	handleResize(e) {
		this.forceUpdate();
	}

	componentDidMount() {
		window.addEventListener('resize', this.handleResize.bind(this));
	}

	componentWillUnmount() {
		window.removeEventListener('resize', this.handleResize.bind(this));
	}

	onChartClick(evt){
		if (evt.currentTarget && evt.currentTarget.layout.hiddenlabels.length != this.state.hiddenlabels.length){
			this.setState({'hiddenlabels': evt.currentTarget.layout.hiddenlabels});
		}
	}
  
  	hasValuesToShow(chartData){
		if (chartData.data[0].type == 'bar'){
			return chartData.data[0].x && chartData.data[0].x.length>0
		} else {
			return chartData.data[0].values && chartData.data[0].values.length>0
		}
	}

	getTotalFormatted(value){
  		if (this.props.chartData.measureType=='projectCount'){
  			return formatValue(value) +" "+translate('chartview.projects');
  		} else {
  			return " PHP "+formatValue(value);
  		}
	}

	getChartInfo(chartType){
		let properties = Object.assign({}, this.props, {hiddenlabels: this.state.hiddenlabels});//add hidden labels to props
		return parseDataChart(chartType, properties, this.refs.chartContainer);
	}

	render() {
		const {measure, chartData, chartType} = this.props;
		const {chartType: chType, itemsToShow, measureType} = chartData;
		let chartInfo;
		if (chartType){
			chartInfo = this.getChartInfo(chartType);
		} else {
			chartInfo = this.getChartInfo(chartData.chartType);
		}
		debugger;
		return (
	    	<div className="chart" ref="chartContainer">
	    		{this.props.title?
		    		<div className="chart-title">
		    			<div className="chart-title-icon"></div>
		    			<div className="chart-title-text">
		    				<div className="title">
		    					{this.props.title || ""}
		    				</div>
		    				<div className="subtitle"> 
		    					{measure? translate('header.settings.'+measure.type) + " " + translate('header.settings.'+measure.measure) : ""}
		    				</div>
		    			</div>		    			
		    		</div>
		    	: null}
	    		{this.props.onChangeItemToShow?
	    			<div className="chart-items-selector">
	    				<Button disabled={itemsToShow > Constants.CHART_ITEMS_STEP_AMOUNT? false : true} onClick={this.setItemsToShow.bind(this, "less")}>
	    					<span>{"<"}</span><span className="less-items">{translate('chartview.less')}</span>
	    				</Button>
	    				<Button disabled={chartData.data && (itemsToShow < chartData.data.length)? false : true} onClick={this.setItemsToShow.bind(this, "more")}>
	    					<span className="more-items">{translate('chartview.more')}</span><span>{">"}</span>
	    				</Button>
	    			</div>
	    		: null}	
	    		{this.props.onChangeType?
	    			<div className="chart-type-selector">
	    				<div className="toggle-button-pair">
						    <div className={chType ==='bar'? "active" : ""} onClick={this.setChartType.bind(this, 'bar')} title={translate('chartview.barchart')}>
						    	<div className={chType ==='bar'? "chart-bar-icon" : "chart-bar-icon-disabled"}></div>
						    </div>
						    <div className={chType ==='pie'? "active" : ""} onClick={this.setChartType.bind(this, 'pie')} title={translate('chartview.piechart')}>
						    	<div className={chType ==='pie'? "chart-pie-icon" : "chart-pie-icon-disabled"}></div>
						    </div>
						</div>	    			    						  
					</div>
	    		: null}  
	    		{this.props.onChangeMeasure?
	    			<div className="chart-measure-selector">
	    				<div className="toggle-button-pair">
						    <div className={measureType ==='funding'? "active" : ""} onClick={this.setMeasType.bind(this, 'funding')} title={translate('chartview.funding')}>
						    	<div className={measureType ==='funding'? "chart-funding-icon" : "chart-funding-icon-disabled"}></div>
						    </div>
						    <div className={measureType ==='projectCount'? "active" : ""} onClick={this.setMeasType.bind(this, 'projectCount')} title={translate('chartview.projectcount')}>
						    	<div className={measureType ==='projectCount'? "chart-projects-icon" : "chart-projects-icon-disabled"}></div>
						    </div>
						</div>		  
					</div>
	    		: null}	
	    		{this.props.showTotalHeader?
	    			<div className="total-funding-chart">{translate('infowindow.tab.totalamount')}: <div>{this.getTotalFormatted(chartInfo.totalAmount)}</div></div>
                : null}	
	    		{!this.hasValuesToShow(chartInfo)?
	    			<div className="no-data">
			    		{translate('chartview.nodata')}
			    	</div>
	    		:
	    			<div>
			      		<Plotly onClick={this.onChartClick.bind(this)} className="" data={chartInfo.data} layout={chartInfo.layout} config={chartInfo.config}/>
			      	</div>	
	    		}
	    		    			  			   	    			      
	      	</div>
	    );
  	}
}

