import React from 'react';
import {Tabs, Tab, Button, Label} from 'react-bootstrap';
import FilterList from './filterListWithSearch'
import FilterDate from './filterDateRange'
import FilterSlider from './filterSliderRange'
import { connect } from 'react-redux'
import translate from '../../util/translate';

class FilterTabContent extends React.Component {

	constructor() {
	    super();
	    this.state = {};
	}

  	componentDidMount() {
		
	}

  	render() {
  		
	    return (
	    	<div className="tab-container">
	    		<Tabs defaultActiveKey={1} animation={false} >
	    			<Tab className="filter-tab-content" eventKey={1} title={translate('filters.funding.funding')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.funding.fundingtypeoda')}>
								<FilterList helpTextKey="help.filters.fundingtab.fundingtype" title="Funding Type (ODA)" filterType="ft" {...this.props.filters["ft"]} />
						    </Tab>
						    <Tab className="filter-list-content" eventKey={2} title={translate('filters.funding.financinginstitutionoda')}>
								<FilterList helpTextKey="help.filters.fundingtab.financinginstitution" title="Financing Institution (ODA)" filterType="fa" {...this.props.filters["fa"]} />
						    </Tab>
						    <Tab className="filter-list-content" eventKey={3} title={translate('filters.funding.fundingclassification')}>
								<FilterList helpTextKey="help.filters.fundingtab.fundingclassification" title="Funding Classification" filterType="cl" {...this.props.filters["cl"]} />
						    </Tab>
						</Tabs>
	                </Tab>
	                <Tab className="filter-tab-content" eventKey={2} title={translate('filters.agency.agency')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.agency.implementingagency')}>
								<FilterList helpTextKey="help.filters.agencytab.fundingagency" title="Implementing Agency" filterType="ia" {...this.props.filters["ia"]} />
						    </Tab>						   
						</Tabs>
	                </Tab>
	                <Tab className="filter-tab-content" eventKey={3} title={translate('filters.sectors.sectors')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.sectors.sector')}>
								<FilterList helpTextKey="help.filters.sectortab.sector" title="Sector" filterType="st" {...this.props.filters["st"]} />
						    </Tab>
						    <Tab className="filter-list-content" eventKey={2} title={translate('filters.sectors.relevanceclimate')}>
								<FilterList helpTextKey="help.filters.sectortab.relevanceclimate" title="Relevance to Climate" filterType="cc" {...this.props.filters["cc"]}  showCode={true}/>
						    </Tab>
						    <Tab className="filter-list-content" eventKey={3} title={translate('filters.sectors.gender')}>
								<FilterList helpTextKey="help.filters.sectortab.gender" title="Gender" filterType="gr" {...this.props.filters["gr"]} showCode={true}/>
						    </Tab>
						</Tabs>
	                </Tab>
	                <Tab className="filter-tab-content" eventKey={4} title={translate('filters.dates.dates')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.dates.implementperiodstart')}>
								<FilterDate filterType="dt_start" helpTextKey="help.filters.datetab.implementperiodstart"
									lang={this.props.language.lan} {...this.props.filters["dt_start"]}
									dateMin={this.props.filters["dt_start"]? this.props.filters["dt_start"].items[1] : ''} 
									dateMax={this.props.filters["dt_start"]? this.props.filters["dt_start"].items[0] : ''}/>
						    </Tab>
						    <Tab className="filter-list-content" eventKey={2} title={translate('filters.dates.implementperiodend')}>
								<FilterDate filterType="dt_end" helpTextKey="help.filters.datetab.implementperiodend"
									lang={this.props.language.lan} {...this.props.filters["dt_end"]}
									dateMin={this.props.filters["dt_end"]? this.props.filters["dt_end"].items[3] : ''} 
									dateMax={this.props.filters["dt_end"]? this.props.filters["dt_end"].items[2] : ''}/>
						    </Tab>
						    <Tab className="filter-list-content" eventKey={3} title={translate('filters.dates.validityperiodstart')}>
								<FilterDate filterType="pp_start" helpTextKey="help.filters.datetab.validityperiodstart"
									lang={this.props.language.lan} {...this.props.filters["pp_start"]}
									dateMin={this.props.filters["pp_start"]? this.props.filters["pp_start"].items[1] : ''} 
									dateMax={this.props.filters["pp_start"]? this.props.filters["pp_start"].items[0] : ''}/>
						    </Tab>
						    <Tab className="filter-list-content" eventKey={4} title={translate('filters.dates.validityperiodend')}>
								<FilterDate filterType="pp_end" helpTextKey="help.filters.datetab.validityperiodend"
									lang={this.props.language.lan} {...this.props.filters["pp_end"]}
									dateMin={this.props.filters["pp_end"]? this.props.filters["pp_end"].items[3] : ''} 
									dateMax={this.props.filters["pp_end"]? this.props.filters["pp_end"].items[2] : ''}/>
						    </Tab>
						</Tabs>
	                </Tab>
	                <Tab className="filter-tab-content" eventKey={5} title={translate('filters.status.status')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.status.financingstatus')}>
								<FilterList helpTextKey="help.filters.statustab.financingstatus" title="Financing Status" filterType="sa" {...this.props.filters["sa"]}/>
						    </Tab>
						</Tabs>
	                </Tab>
	                <Tab className="filter-tab-content" eventKey={6} title={translate('filters.financialamount.financialamount')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.financialamount.financialamount')}>
								<FilterSlider helpTextKey="help.filters.financialtab.financialamount" filterType="fin_amount" valueSymbolPre="₱" logMarks={true} {...this.props.filters["fin_amount"]} 
									valueMin={this.props.filters["fin_amount"]? parseInt(this.props.filters["fin_amount"].items[1]) : 0} 
									valueMax={this.props.filters["fin_amount"]? parseInt(this.props.filters["fin_amount"].items[0]) : 100}/>
						    </Tab>
						</Tabs>
	                </Tab>
	                <Tab className="filter-tab-content" eventKey={7} title={translate('filters.physical.physicalandfinancial')}>
	                  	<Tabs defaultActiveKey={1} animation={false} position="left" tabWidth={3}>
							<Tab className="filter-list-content" eventKey={1} title={translate('filters.physical.physicalProgress')}>
								<FilterSlider helpTextKey="help.filters.physicaltab.physicalprogress" filterType="php" valueSymbolPost="%" {...this.props.filters["php"]} 
									valueMin={this.props.filters["php"]? parseInt(this.props.filters["php"].items[1]) : 0} 
									valueMax={this.props.filters["php"]? parseInt(this.props.filters["php"].items[0]) : 100}/>
						    </Tab>
						    <Tab className="filter-list-content" eventKey={2} title={translate('filters.physical.physicalstatus')}>
								<FilterList helpTextKey="help.filters.physicaltab.physicalstatus" title="Physical Status" filterType="ph" {...this.props.filters["ph"]} />
						    </Tab>
						</Tabs>
	                </Tab>
				</Tabs>
            </div>
	    );
	   
  	}
}

const mapStateToProps = (state, props) => {
  return {
    filters: state.filters.filterMain, language: state.language
  }
}

export default connect(mapStateToProps)(FilterTabContent);;
