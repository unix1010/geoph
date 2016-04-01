import React from 'react';
import { connect } from 'react-redux'
import DatePicker from 'react-date-picker'
import Moment from 'moment'
import { setFilterRange, fetchFilterDataIfNeeded } from '../../actions/filters.js'


class FilterDate extends React.Component {

	constructor() {
	    super();
	    this.state = {'errorMessage': '', 'dateNow': Date.now()};
	}

	componentDidMount() {
		this.props.onLoadFilterData(this.props.filterType);		
	}

	handleStartDate(date) {
		let sd = Moment(date);
		let ed = this.refs.endDate.getDate();//.format("YYYY-MM-DD");	
		if (this.validateDates(sd, ed)){
			this.props.onDateChange({filterType: this.props.filterType, minSelected:date, maxSelected: ed.format("YYYY-MM-DD")});
		}
	}

  	handleEndDate(date) {
		let ed = Moment(date);
		let sd = this.refs.startDate.getDate();//.format("YYYY-MM-DD");	
		if (this.validateDates(sd, ed)){
			this.props.onDateChange({filterType: this.props.filterType, minSelected: sd.format("YYYY-MM-DD"), maxSelected: date});
		}
	}

	validateDates(startDate, endDate){
		if (startDate.isAfter(endDate) || endDate.isBefore(startDate)){
			this.setState({'errorMessage': 'End Date should be greater than Start Date'});
			return false;
		} else {
			this.setState({'errorMessage': ''});
			return true;
		}
	}

  	render() {
  		let startMinDate = this.props.items? this.props.items[0].minDate : '';
  		let startMaxDate = this.props.maxSelected || (this.props.items? this.props.items[0].maxDate : '');
  		let endMinDate = this.props.minSelected || (this.props.items? this.props.items[0].minDate : '');
  		let endMaxDate = this.props.items? this.props.items[0].maxDate : '';
  		return (
	        <div className="date-picker-container">
	        	<div className="date-picker-div">
	        		<span>Start Date: <b>{this.props.minSelected || "Not Set"}</b></span>
	        		<DatePicker 
	        			hideFooter={true}
	        			ref="startDate" 
	        			locale={this.props.lang} 
	        			minDate={startMinDate} 
	        			maxDate={startMaxDate} 
	        			date={this.props.minSelected || this.state.dateNow} 
	        			onChange={this.handleStartDate.bind(this)} />
		        </div>	
		        <div className="date-picker-div">
	        		<span>End Date: <b>{this.props.maxSelected || "Not Set"}</b></span>
	        		<DatePicker 
	        			hideFooter={true}
	        			ref="endDate" 
	        			locale={this.props.lang} 
	        			minDate={endMinDate} 
	        			maxDate={endMaxDate} 
	        			date={this.props.maxSelected || this.state.dateNow} 
	        			onChange={this.handleEndDate.bind(this)} />	 
		        </div>
		        {this.state.errorMessage.length>0?
					<div className="error-message">
		        		<b>Error: </b>
		        		{this.state.errorMessage}
			        </div>
		        : null}      
	        </div>
      	);
  	}
}


const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    onLoadFilterData: (type) => {
      dispatch(fetchFilterDataIfNeeded(type));
    },

    onDateChange: (filterRange) => {
      dispatch(setFilterRange(filterRange));
    }
  }
}

export default connect(null,mapDispatchToProps)(FilterDate);
