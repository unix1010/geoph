import React from 'react';
import i18next from 'i18next';

export default class Translator extends React.Component {
	render(){
		const e=i18next;
		return <span className={this.props.className}>
			
			{i18next.t(this.props.k,this.props.lan)}

		</span>		
	}
}