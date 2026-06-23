import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { GroupService } from '../../services/group.service';

@Component({
  selector: 'app-group-view-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './group-view-boundary.component.html',
  styleUrl: './group-view-boundary.component.css'
})
export class GroupViewBoundaryComponent implements OnInit {
  group: any = null;
  errorMessage: string = '';

  constructor(private route: ActivatedRoute, private groupService: GroupService) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadGroup(Number(id));
      }
    });
  }

  loadGroup(id: number) {
    this.groupService.getGroup(id).subscribe({
      next: (data) => this.group = data,
      error: () => this.errorMessage = 'Errore durante il caricamento del gruppo o permessi insufficienti.'
    });
  }
}
